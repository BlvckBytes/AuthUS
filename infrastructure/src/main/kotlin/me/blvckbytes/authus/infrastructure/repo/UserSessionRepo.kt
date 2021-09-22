package me.blvckbytes.authus.infrastructure.repo

import io.jsonwebtoken.*
import me.blvckbytes.authus.domain.exception.InvalidAuthorizationException
import me.blvckbytes.authus.domain.exception.InvalidCredentialsException
import me.blvckbytes.authus.domain.exception.ModelNotFoundException
import me.blvckbytes.authus.domain.model.UserAccountModel
import me.blvckbytes.authus.domain.model.UserCredentialsModel
import me.blvckbytes.authus.domain.model.UserSessionModel
import me.blvckbytes.authus.domain.repo.port.IUserAccountRepo
import me.blvckbytes.authus.domain.repo.port.IUserSessionRepo
import me.blvckbytes.authus.infrastructure.entity.auxiliary.InvalidatedToken
import me.blvckbytes.authus.infrastructure.table.InvalidatedTokens
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.core.env.Environment
import org.springframework.stereotype.Repository
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Repository
open class UserSessionRepo(
    @Autowired @Lazy private val accountRepo: IUserAccountRepo,
    @Autowired @Lazy private val env: Environment
) : IUserSessionRepo {

    // Property file values regarding JWT
    private val jwtSecret = env.getProperty("jwtconf.secret") ?: "too-weak"
    private val jwtRefreshSecret = env.getProperty("jwtconf.refresh_secret") ?: "too-weak"
    private val jwtIssuer = env.getProperty("jwtconf.issuer") ?: "AuthService"
    private val jwtTTL = (env.getProperty("jwtconf.ttl") ?: "3600").toInt()
    private val jwtRefreshTTL = (env.getProperty("jwtconf.refresh_ttl") ?: "18000").toInt()

    override fun createSession(credentials: UserCredentialsModel): UserSessionModel {
        // Try get the account responding to this username
        // Mapping the not-found exception to invalid-credentials to avoid username-bruteforcing
        val acc: UserAccountModel
        try {
            acc = accountRepo.getAccount(credentials.username)
        } catch (ex: ModelNotFoundException) {
            throw InvalidCredentialsException(credentials.username)
        }

        // Password did not match
        if (!acc.checkPassword(credentials.password))
            throw InvalidCredentialsException(credentials.username)

        return createSession(acc)
    }

    override fun parseAccessToken(accessToken: String?): UserSessionModel {
        val claims = parseClaims(accessToken, jwtSecret)
        val lastAccountUpdate = DateTime(claims.get("lau", Date::class.java))
        val hai = UUID.fromString(claims.get("haid", String::class.java))

        // Hash has been changed, this token is invalid
        if (!accountRepo.isStampLatest(hai, lastAccountUpdate))
            throw InvalidAuthorizationException()

        // Parse metadata
        val apn = claims.get("apn", List::class.java).map { it.toString() }

        // Build session from data
        return UserSessionModel(
            UUID.fromString(claims.id), DateTime(claims.issuedAt), DateTime(claims.expiration),
            hai, apn, lastAccountUpdate, accessToken!!, null
        )
    }

    override fun refreshSession(refreshToken: String?): UserSessionModel {
        // Get holder account ID from claims
        val claims = parseClaims(refreshToken, jwtRefreshSecret)
        val holder = claims.get("haid", String::class.java)
        val acc = accountRepo.getAccount(UUID.fromString(holder))

        // Invalidate this refresh token
        invalidateToken(UUID.fromString(claims.id), DateTime(claims.expiration))

        // Invalidate access token from refresh token info
        val accessTokenId = claims.get("atid", String::class.java)
        val accessTokenExpiry = claims.get("atexp", Date::class.java)
        invalidateToken(UUID.fromString((accessTokenId)), DateTime(accessTokenExpiry))

        // Create new session
        return createSession(acc)
    }

    override fun destroySession(session: UserSessionModel) {
        transaction {
            // Already destroyed, do nothing
            if (isTokenInvalidated(session.id))
                return@transaction

            // Invalidate access token
            val accessClaims = parseClaims(session.accessToken, jwtSecret)
            invalidateToken(
                UUID.fromString(accessClaims.id),
                DateTime(accessClaims.expiration)
            )

            // Invalidate refresh token
            val refreshClaims = parseClaims(session.refreshToken, jwtRefreshSecret)
            invalidateToken(
                UUID.fromString(refreshClaims.id),
                DateTime(refreshClaims.expiration)
            )
        }
    }

    private fun isTokenInvalidated(id: UUID): Boolean {
        return transaction {
            InvalidatedTokens.select { InvalidatedTokens.tokenId eq id }.count() > 0
        }
    }

    private fun deleteExpiredInvalidations() {
        transaction {
            InvalidatedTokens.deleteWhere {
                // Invalidated itself, thus no need to invalidate it artificially anymore
                InvalidatedTokens.validUntil.less(DateTime.now())
            }
        }
    }

    private fun invalidateToken(tokenId: UUID, validUntil: DateTime) {
        transaction {
            // Clean up expired invalidation entries, this will keep the data at a minimum
            deleteExpiredInvalidations()

            // Already invalid, no need to invalidate through DB
            if (validUntil.isBeforeNow)
                return@transaction

            // Create new invalidation entry
            InvalidatedToken.new {
                this.tokenId = tokenId
                this.validUntil = validUntil
            }
        }
    }

    private fun createSession(acc: UserAccountModel): UserSessionModel {
        // Issued now and valid until now plus JWT's TTL
        val issuedAt = DateTime.now()
        val validUntil = issuedAt.plusSeconds(jwtTTL)
        val refreshValidUntil = issuedAt.plusSeconds(jwtRefreshTTL)

        // Get all active permission nodes
        val apn = accountRepo.listEffectivePermissionNodes(acc.id)

        // Create both token IDs beforehand
        val accessTokenId = UUID.randomUUID()
        val refreshTokenId = UUID.randomUUID()

        val refreshToken = createRefreshToken(refreshTokenId, issuedAt, refreshValidUntil, acc.id, accessTokenId, validUntil)
        val accessToken = createAccessToken(accessTokenId, issuedAt, validUntil, acc.id, apn, acc.updatedAt, refreshTokenId, refreshValidUntil)

        // Create session from data
        return UserSessionModel(
            accessTokenId, issuedAt, validUntil, acc.id, apn, acc.updatedAt, accessToken, refreshToken
        )
    }

    private fun createRefreshToken(
        id: UUID,
        issuedAt: DateTime,
        validUntil: DateTime,
        holderAccountId: UUID,
        accessTokenId: UUID,
        accessTokenExpiry: DateTime
    ): String {
        val signatureAlgorithm = SignatureAlgorithm.HS256
        val signingKey = SecretKeySpec(jwtRefreshSecret.toByteArray(), signatureAlgorithm.jcaName)

        return Jwts.builder()
            .setId(id.toString())
            .setIssuedAt(issuedAt.toDate())
            .setIssuer(jwtIssuer)
            .signWith(signingKey, signatureAlgorithm)
            .setExpiration(validUntil.toDate())

            // Holder account-ID claim
            .claim("haid", holderAccountId)

            // Access token ID
            .claim("atid", accessTokenId)

            // Access token expiry
            .claim("atexp", accessTokenExpiry.toDate())

            .compact()
    }

    private fun createAccessToken(
        id: UUID,
        issuedAt: DateTime,
        validUntil: DateTime,
        holderAccountId: UUID,
        activePermissionNodes: List<String>,
        lastAccountUpdate: DateTime,
        refreshTokenId: UUID,
        refreshTokenExpiry: DateTime
    ): String {
        val signatureAlgorithm = SignatureAlgorithm.HS256
        val signingKey = SecretKeySpec(jwtSecret.toByteArray(), signatureAlgorithm.jcaName)

        // Build JWT with random UUID, dates, signed and containing custom claims
        return Jwts.builder()
            .setId(id.toString())
            .setIssuedAt(issuedAt.toDate())
            .setIssuer(jwtIssuer)
            .signWith(signingKey, signatureAlgorithm)
            .setExpiration(validUntil.toDate())

            // Holder account-ID
            .claim("haid", holderAccountId)

            // Active permission nodes
            .claim("apn", activePermissionNodes)

            // Last account update
            .claim("lau", lastAccountUpdate.toDate())

            // Refresh token ID
            .claim("atid", refreshTokenId)

            // Refresh token expiry
            .claim("atexp", refreshTokenExpiry.toDate())

            // To bearer-value
            .compact()
    }

    private fun parseClaims(bearerToken: String?, signingKey: String): Claims {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(signingKey.toByteArray())
                .build().parseClaimsJws(bearerToken).body

            val id = UUID.fromString(claims.id)

            // Token has been invalidated
            if (isTokenInvalidated(id))
                throw InvalidAuthorizationException()

            claims
        } catch (ex: Exception) {
            @Suppress("DEPRECATION")
            when (ex) {
                // Map expected issues to invalid auth
                is IllegalArgumentException, is MalformedJwtException,
                is SignatureException, is SecurityException, is ExpiredJwtException -> throw InvalidAuthorizationException()

                else -> throw ex
            }
        }
    }
}
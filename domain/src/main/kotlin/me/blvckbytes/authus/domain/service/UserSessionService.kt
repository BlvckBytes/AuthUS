package me.blvckbytes.authus.domain.service

import me.blvckbytes.authus.domain.model.UserCredentialsModel
import me.blvckbytes.authus.domain.model.UserSessionModel
import me.blvckbytes.authus.domain.repo.port.IUserAccountRepo
import me.blvckbytes.authus.domain.repo.port.IUserSessionRepo
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserSessionService(
    @Autowired private val sessionRepo: IUserSessionRepo,
    @Autowired private val userAccountRepo: IUserAccountRepo
) {

    fun createSession(credentials: UserCredentialsModel): UserSessionModel {
        return sessionRepo.createSession(credentials)
    }

    fun refreshSession(refreshToken: String?): UserSessionModel {
        return sessionRepo.refreshSession(refreshToken)
    }

    fun parseAccessToken(jwt: String): UserSessionModel {
        return sessionRepo.parseAccessToken(jwt)
    }

    fun isStampLatest(id: UUID, stamp: DateTime): Boolean {
        return userAccountRepo.isStampLatest(id, stamp)
    }

    fun destroySession(session: UserSessionModel) {
        sessionRepo.destroySession(session)
    }
}
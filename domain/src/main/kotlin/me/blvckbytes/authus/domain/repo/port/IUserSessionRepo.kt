package me.blvckbytes.authus.domain.repo.port

import me.blvckbytes.authus.domain.model.UserCredentialsModel
import me.blvckbytes.authus.domain.model.UserSessionModel

/**
 * Responsible for creating tokens from credentials (login-attempts) and
 * parsing the header-value back into a session
 */
interface IUserSessionRepo {

    /**
     * Creates a new session, based on the login credentials provided
     * @param credentials Credentials used to log in
     */
    fun createSession(credentials: UserCredentialsModel): UserSessionModel

    /**
     * Parses the access-token value back into a session
     * @param accessToken Valid bearer-token value, raises errors if null or malformed
     * @return Parsed session
     */
    fun parseAccessToken(accessToken: String?): UserSessionModel

    /**
     * Creates a new session, based on the refresh token handed out with
     * the previously created session
     * @param refreshToken Valid refresh token to prove identity, raises errors if null or malformed
     */
    fun refreshSession(refreshToken: String?): UserSessionModel

    /**
     * Destroys the provided session's tokens so it cannot be used ever again
     * @param session Session to invalidate
     */
    fun destroySession(session: UserSessionModel)
}
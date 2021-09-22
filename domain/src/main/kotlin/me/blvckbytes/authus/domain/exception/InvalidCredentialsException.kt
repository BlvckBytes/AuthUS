package me.blvckbytes.authus.domain.exception

/**
 * Signals that the provided credentials do not match the account
 * @param username Username of the requested account
 */
class InvalidCredentialsException(
    private val username: String
) : ASimpleException() {

    override fun toMessage(): String {
        return "These credentials do not match the account of '${username}'"
    }
}
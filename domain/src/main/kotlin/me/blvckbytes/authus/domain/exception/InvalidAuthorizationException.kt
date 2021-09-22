package me.blvckbytes.authus.domain.exception

/**
 * Signals that there has been either no or just an invalid authorization provided
 */
class InvalidAuthorizationException : ASimpleException() {

    override fun toMessage(): String {
        return "This endpoint requires a valid authorization"
    }
}
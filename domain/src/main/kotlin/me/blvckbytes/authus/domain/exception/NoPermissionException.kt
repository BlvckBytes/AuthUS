package me.blvckbytes.authus.domain.exception

/**
 * Signals that there is a lack of permission for a certain action desired to be performed
 */
class NoPermissionException(
    private val node: String
): ASimpleException() {

    override fun toMessage(): String {
        return "You lack the permission '$node' to be able to do this!"
    }
}
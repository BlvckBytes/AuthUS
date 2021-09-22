package me.blvckbytes.authus.domain.exception

/**
 * Signals that the order has not been complete or tampered with
 */
class IllegalOrderException : ASimpleException() {

    override fun toMessage(): String {
        return "The sort-request did not (just) contain all existing IDs"
    }
}
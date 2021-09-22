package me.blvckbytes.authus.domain.exception

/**
 * Signals that the ID requested was not parsable as the internal format used
 * @param id ID with wrong format
 */
class InvalidIdException(
    val id: String
) : ASimpleException() {

    override fun toMessage(): String {
        return "The identifier '$id' is invalid and could not be parsed"
    }
}
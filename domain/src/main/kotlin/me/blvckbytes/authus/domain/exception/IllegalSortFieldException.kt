package me.blvckbytes.authus.domain.exception

/**
 * Signals that a field which should be sorted by is non-existing
 * @param field Field that doesn't exist
 */
class IllegalSortFieldException(
    private val field: String
) : ASimpleException() {

    override fun toMessage(): String {
        return "The sort request's field '$field' is not supported or not existing"
    }
}
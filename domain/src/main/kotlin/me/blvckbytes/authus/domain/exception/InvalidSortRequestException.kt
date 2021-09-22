package me.blvckbytes.authus.domain.exception

/**
 * Signals that the provided sort-request was not parsable
 * @param request Sort-request issued
 */
class InvalidSortRequestException(
    private val request: String
) : ASimpleException() {

    override fun toMessage(): String {
        return "The sort request '$request' was not properly specified"
    }
}
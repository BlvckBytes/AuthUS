package me.blvckbytes.authus.domain.exception

/**
 * Signals that the filter request doesn't meet the required rules
 */
class InvalidFilterException(
    private val reason: FilterExceptionReason,
    private val value: String
) : ASimpleException() {

    override fun toMessage(): String {
        return "Could not perform requested filtration, issue: ${reason}: $value"
    }
}
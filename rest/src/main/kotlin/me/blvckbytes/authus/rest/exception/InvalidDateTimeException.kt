package me.blvckbytes.authus.rest.exception

import me.blvckbytes.authus.domain.exception.ASimpleException

class InvalidDateTimeException(
    val value: String
) : ASimpleException() {

    override fun toMessage(): String {
        return "Could not parse DateTime '${value}', please provide ISO-8601 formats only!"
    }
}
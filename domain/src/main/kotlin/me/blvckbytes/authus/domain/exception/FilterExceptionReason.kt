package me.blvckbytes.authus.domain.exception

/**
 * Used to signal the distinct reason why an [InvalidFilterException] was raised
 */
enum class FilterExceptionReason {
    // Syntax is off
    INVALID_FORMAT,

    // This connection between operations doesn't exist
    INVALID_CONNECTION,

    // There is no such operation
    INVALID_OPERATION,

    // This operation doesn't support null-values
    NOT_NULLABLE,

    // The type of this field cannot take this operation
    UNSUPPORTED_OPERATION,

    // Value provided was not parsable into field's datatype
    NOT_PARSABLE,

    // This field does not support filtering
    UNSUPPORTED_FIELD
}
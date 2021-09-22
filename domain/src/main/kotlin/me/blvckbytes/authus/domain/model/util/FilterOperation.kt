package me.blvckbytes.authus.domain.model.util

/**
 * A filter operation can be applied "between" a field of a model and a
 * concrete value, like a string or a number
 */
enum class FilterOperation {
    // Equal
    EQ,

    // Not equal
    NEQ,

    // Less than or equal
    LTE,

    // Less than
    LT,

    // Greater than or equal
    GTE,

    // Greater than
    GT,

    // Contains
    CNT
}
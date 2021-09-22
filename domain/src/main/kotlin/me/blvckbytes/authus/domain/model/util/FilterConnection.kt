package me.blvckbytes.authus.domain.model.util

/**
 * A filter connection can be applied once to a certain field of a model,
 * connecting all following operations with this logic gate
 */
enum class FilterConnection {
    // All of the operations need to be fulfilled
    AND,

    // Any of the operations need to be fulfilled
    OR
}
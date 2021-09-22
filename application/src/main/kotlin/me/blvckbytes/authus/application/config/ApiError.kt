package me.blvckbytes.authus.application.config

/**
 * The format this API will respond errors in
 * @param message Main error message providing informations about the problem
 * @param detail List of details to further clarify what's wrong
 */
class ApiError(
    var message: String,
    var detail: List<String>? = emptyList()
)
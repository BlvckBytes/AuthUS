package me.blvckbytes.authus.domain.exception

/**
 * Signals that there is no model corresponding to the requested ID
 * @param model Name of the model in charge
 * @param id ID of the target
 */
class ModelNotFoundException(
    val model: String,
    val id: String
) : ASimpleException() {

    override fun toMessage(): String {
        return "A $model with ID '$id' could not be located"
    }
}
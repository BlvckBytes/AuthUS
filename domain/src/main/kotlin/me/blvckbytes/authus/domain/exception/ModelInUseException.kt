package me.blvckbytes.authus.domain.exception

/**
 * Signals that a model is still in use and cannot be deleted
 * @param model Name of the model in charge
 * @param id ID of the target
 */
class ModelInUseException(
    val model: String,
    val id: String
) : ASimpleException() {

    override fun toMessage(): String {
        return "The $model with ID '$id' is still in use and cannot be deleted"
    }
}
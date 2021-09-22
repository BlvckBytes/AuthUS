package me.blvckbytes.authus.domain.exception

/**
 * Signals that a collision occurred, based on fields that need to be kept unique
 * @param model Name of the model in charge
 * @param field Name of the field colliding
 * @param value Value of the colliding field
 */
class ModelCollisionException(
    private val model: String,
    private val field: String,
    private val value: String
) : ASimpleException() {

    override fun toMessage(): String {
        return "A $model with '$field' of value '$value' already exists"
    }
}
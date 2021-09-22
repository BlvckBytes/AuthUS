package me.blvckbytes.authus.domain.exception

/**
 * The basis of all exceptions, needs to be able to format to a message
 */
abstract class ASimpleException : RuntimeException() {
    abstract fun toMessage(): String
}
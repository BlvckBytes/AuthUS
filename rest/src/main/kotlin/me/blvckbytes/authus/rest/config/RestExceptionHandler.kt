package me.blvckbytes.authus.rest.config

import me.blvckbytes.authus.domain.exception.*
import me.blvckbytes.authus.rest.exception.InvalidDateTimeException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import kotlin.reflect.KClass

@ControllerAdvice
class RestExceptionHandler : ResponseEntityExceptionHandler() {

    companion object {
        // Fallback code if the custom exception has not been mapped explicitly
        private val statusCodeFallback = HttpStatus.BAD_REQUEST

        // Mapping custom exceptions to custom status codes
        private val statusCodes = mapOf<KClass<*>, HttpStatus>(
            ModelNotFoundException::class to HttpStatus.NOT_FOUND,
            ModelCollisionException::class to HttpStatus.CONFLICT,
            InvalidAuthorizationException::class to HttpStatus.UNAUTHORIZED,
            InvalidCredentialsException::class to HttpStatus.UNAUTHORIZED,
            ModelInUseException::class to HttpStatus.CONFLICT,
            NoPermissionException::class to HttpStatus.FORBIDDEN,
            InvalidDateTimeException::class to HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(Exception::class)
    private fun handleGenericException(ex: Exception): ResponseEntity<Any> {
        if (ex is ASimpleException)
            return ResponseEntity.status(statusCodes[ex::class] ?: statusCodeFallback).body(ApiError(ex.toMessage()))

        // Not a custom exception, these are internal server errors
        ex.printStackTrace()
        return ResponseEntity.status(500).body(ApiError("An internal error occurred"))
    }

    // HTTP-Request not parsable
    override fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        return handleKnownException(ex, ResponseEntity.badRequest().body(ApiError("The request-body could not be parsed (bad semantics)")))
    }

    // HTTP-Method not supported on this path
    override fun handleHttpRequestMethodNotSupported(ex: HttpRequestMethodNotSupportedException, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        return handleKnownException(ex, ResponseEntity(ApiError("This endpoint does not support the ${ex.method} method"), HttpStatus.METHOD_NOT_ALLOWED))
    }

    // Parameters within the request were not valid (restriction validation)
    override fun handleBindException(ex: BindException, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        return handleKnownException(ex, ResponseEntity.badRequest().body(ApiError("The request-parameters did not pass validation", convertBindException(ex))))
    }

    // Fields within the body were not valid (restriction validation)
    override fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        return handleKnownException(ex, ResponseEntity.badRequest().body(ApiError("The request-body did not pass validation", convertBindException(ex.bindingResult))))
    }

    /**
     * Helper method to convert a binding-result to a list of errors
     * regarding the field and the concrete issue
     */
    private fun convertBindException(result: BindingResult): List<String> {
        return result.fieldErrors.map { "${it.field}: ${it.defaultMessage}" } +
                result.globalErrors.map { "${it.objectName}: ${it.defaultMessage}" }
    }

    /**
     * Helper method to handle known exceptions, checking by walking
     * up the whole cause reference chain. If exception is known, calls
     * @see handleGenericException and responds with that result, otherwise
     * responds with the provided fallback
     */
    private fun handleKnownException(ex: Exception, fallback: ResponseEntity<Any>): ResponseEntity<Any> {
        // Search in chain for known exception
        var currCause = ex.cause
        do {
            if (currCause is ASimpleException)
                return handleGenericException(currCause)
            currCause = currCause?.cause
        } while (currCause != null)

        // Nothing known in chain
        return fallback
    }
}
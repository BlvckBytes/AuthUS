package me.blvckbytes.authus.application.config

import me.blvckbytes.authus.domain.exception.InvalidAuthorizationException
import me.blvckbytes.authus.domain.exception.InvalidIdException
import me.blvckbytes.authus.domain.model.UserSessionModel
import me.blvckbytes.authus.domain.service.UserSessionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.*

abstract class AControllerBase(
    private val sessionService: UserSessionService
) {

    /**
     * Parse a token directly from the auth-header content
     * @param authHeader Header's content, format: Bearer <value>
     */
    protected fun parseToken(authHeader: String?): UserSessionModel {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw InvalidAuthorizationException()
        return sessionService.parseAccessToken(authHeader.split(" ", limit = 2)[1])
    }

    /**
     * Transform a boolean into the appropriate response-entity
     */
    protected fun booleanResponse(value: Boolean): ResponseEntity<Any> {
        return ResponseEntity.status(
            if (value) HttpStatus.NO_CONTENT else HttpStatus.NOT_FOUND
        ).build()
    }

    /**
     * Try to parse a UUID, raises an exception if malformed
     * @param input ID as a string
     */
    protected fun parseUUID(input: String): UUID {
        try {
            return UUID.fromString(input)
        } catch (ex: IllegalArgumentException) {
            throw InvalidIdException(input)
        }
    }
}
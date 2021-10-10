package me.blvckbytes.authus.rest.controller

import me.blvckbytes.authus.rest.config.AControllerBase
import me.blvckbytes.authus.rest.dto.SessionRefreshRequestDTO
import me.blvckbytes.authus.rest.dto.UserCredentialsDTO
import me.blvckbytes.authus.rest.dto.UserSessionDTO
import me.blvckbytes.authus.rest.dto.toDTO
import me.blvckbytes.authus.domain.service.UserSessionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/sessions")
class UserSessionController(
    @Autowired
    private val sessionService: UserSessionService,
) : AControllerBase(sessionService) {

    @PostMapping
    fun createSession(
        @Valid @RequestBody credentials: UserCredentialsDTO
    ): ResponseEntity<UserSessionDTO> {
        val sess = sessionService.createSession(credentials.toModel())
        return ResponseEntity.ok(sess.toDTO())
    }

    @PostMapping("/refresh")
    fun refreshSession(
        @Valid @RequestBody refreshRequest: SessionRefreshRequestDTO
    ): ResponseEntity<UserSessionDTO> {
        val sess = sessionService.refreshSession(refreshRequest.refresh_token)
        return ResponseEntity.ok(sess.toDTO())
    }

    @DeleteMapping
    fun destroySession(
        @RequestHeader("Authorization") auth: String?
    ): ResponseEntity<Any> {
        sessionService.destroySession(parseToken(auth))
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getSession(
        @RequestHeader("Authorization") auth: String?
    ): ResponseEntity<UserSessionDTO> {
        return ResponseEntity.ok(parseToken(auth).toDTO())
    }
}
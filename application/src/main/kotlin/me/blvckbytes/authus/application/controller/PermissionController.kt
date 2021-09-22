package me.blvckbytes.authus.application.controller

import me.blvckbytes.authus.application.config.AControllerBase
import me.blvckbytes.authus.application.config.PageCursorParam
import me.blvckbytes.authus.application.dto.ListResponseDTO
import me.blvckbytes.authus.application.dto.PermissionDTO
import me.blvckbytes.authus.application.dto.toDTO
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import me.blvckbytes.authus.domain.service.PermissionService
import me.blvckbytes.authus.domain.service.UserSessionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.validation.Valid

@RestController
@RequestMapping("/permissions")
class PermissionController(
    @Autowired private val permissionService: PermissionService,
    @Autowired private val sessionService: UserSessionService
): AControllerBase(sessionService) {

    @GetMapping("/{id}")
    fun getPermission(
        @PathVariable id: String
    ): ResponseEntity<PermissionDTO> {
        return ResponseEntity.ok(permissionService.getPermission(parseUUID(id)).toDTO())
    }

    @PostMapping
    fun createPermission(
        @Valid @RequestBody permission: PermissionDTO
    ): ResponseEntity<PermissionDTO> {
        val created = permissionService.createPermission(permission.toModel())
        return ResponseEntity.created(
            ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id)
                .toUri()
        ).body(created.toDTO())
    }

    @PutMapping("/{id}")
    fun changePermission(
        @PathVariable id: String,
        @Valid @RequestBody permission: PermissionDTO
    ): ResponseEntity<PermissionDTO> {
        return ResponseEntity.ok(permissionService.updatePermission(parseUUID(id), permission.toModel()).toDTO())
    }

    @DeleteMapping("/{id}")
    fun deletePermission(
        @PathVariable id: String
    ): ResponseEntity<Any> {
        permissionService.deletePermission(parseUUID(id))
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun listPermissions(
        @PageCursorParam cursor: PageCursorModel
    ): ResponseEntity<ListResponseDTO<PermissionDTO>> {
        val res = permissionService.listPermissions(cursor)
        return ResponseEntity.ok(ListResponseDTO(res.first.map { it.toDTO() }, res.second.toDTO()))
    }
}
package me.blvckbytes.authus.rest.controller

import me.blvckbytes.authus.rest.config.AControllerBase
import me.blvckbytes.authus.rest.config.PageCursorParam
import me.blvckbytes.authus.rest.dto.*
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import me.blvckbytes.authus.domain.service.GroupService
import me.blvckbytes.authus.domain.service.UserSessionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.validation.Valid

@RestController
@RequestMapping("/groups")
class GroupController(
    @Autowired private val groupService: GroupService,
    @Autowired private val sessionService: UserSessionService
): AControllerBase(sessionService) {

    /////////////////////////////////////////////
    //              Group CRUD                 //
    /////////////////////////////////////////////

    @GetMapping("/{id}")
    fun getGroup(
        @PathVariable id: String
    ): ResponseEntity<GroupDTO> {
        return ResponseEntity.ok(groupService.getGroup(parseUUID(id)).toDTO())
    }

    @PostMapping
    fun createGroup(
        @Valid @RequestBody group: GroupDTO
    ): ResponseEntity<GroupDTO> {
        val created = groupService.createGroup(group.toModel())
        return ResponseEntity.created(
            ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id)
                .toUri()
        ).body(created.toDTO())
    }

    @PutMapping("/{id}")
    fun changeGroup(
        @PathVariable id: String,
        @Valid @RequestBody group: GroupDTO
    ): ResponseEntity<GroupDTO> {
        return ResponseEntity.ok(groupService.updateGroup(parseUUID(id), group.toModel()).toDTO())
    }

    @DeleteMapping("/{id}")
    fun deleteGroup(
        @PathVariable id: String
    ): ResponseEntity<Any> {
        groupService.deleteGroup(parseUUID(id))
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getGroups(
        @PageCursorParam cursor: PageCursorModel
    ): ResponseEntity<ListResponseDTO<GroupDTO>> {
        val res = groupService.listGroups(cursor)
        return ResponseEntity.ok(ListResponseDTO(res.first.map { it.toDTO() }, res.second.toDTO()))
    }

    /////////////////////////////////////////////
    //       Group Permission Management       //
    /////////////////////////////////////////////

    @GetMapping("/{id}/permissions/{pid}")
    fun getPermission(
        @PathVariable id: String,
        @PathVariable pid: String
    ): ResponseEntity<AssignedPermissionDTO> {
        val res = groupService.getPermission(parseUUID(id), parseUUID(pid))
        return ResponseEntity.ok(res.toDTO())
    }

    @PostMapping("/{id}/permissions")
    fun appendPermission(
        @PathVariable id: String,
        @Valid @RequestBody perm: AssignedPermissionDTO
    ): ResponseEntity<AssignedPermissionDTO> {
        val created = groupService.appendPermission(parseUUID(id), perm.id!!, perm.validUntil, perm.negative ?: false)
        return ResponseEntity.created(
            ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.permission.id)
                .toUri()
        ).body(created.toDTO())
    }

    @PutMapping("/{id}/permissions/{pid}")
    fun removePermission(
        @PathVariable id: String,
        @PathVariable pid: String,
        @Valid @RequestBody perm: AssignedPermissionMetadataDTO
    ): ResponseEntity<AssignedPermissionDTO> {
        val updated = groupService.updatePermission(parseUUID(id), parseUUID(pid), perm.validUntil, perm.negative ?: false)
        return ResponseEntity.ok(updated.toDTO())
    }

    @DeleteMapping("/{id}/permissions/{pid}")
    fun removePermission(
        @PathVariable id: String,
        @PathVariable pid: String
    ): ResponseEntity<Any> {
        groupService.removePermission(parseUUID(id), parseUUID(pid))
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/permissions")
    fun listPermissions(
        @PathVariable id: String,
        @PageCursorParam cursor: PageCursorModel
    ): ResponseEntity<ListResponseDTO<AssignedPermissionDTO>> {
        val res = groupService.listPermissions(parseUUID(id), cursor)
        return ResponseEntity.ok(ListResponseDTO(res.first.map { it.toDTO() }, res.second.toDTO()))
    }

    @GetMapping("/{id}/has-permission/{pid}")
    fun hasPermission(
        @PathVariable id: String,
        @PathVariable pid: String
    ): ResponseEntity<Any> {
        return booleanResponse(groupService.hasPermission(parseUUID(id), parseUUID(pid)))
    }

    /////////////////////////////////////////////
    //       Group Inheritance Management      //
    /////////////////////////////////////////////

    @GetMapping("/{id}/parents/{pid}")
    fun getParent(
        @PathVariable id: String,
        @PathVariable pid: String
    ): ResponseEntity<GroupMembershipDTO> {
        return ResponseEntity.ok(groupService.getParent(parseUUID(id), parseUUID(pid)).toDTO())
    }

    @PostMapping("/{id}/parents")
    fun addParent(
        @PathVariable id: String,
        @Valid @RequestBody parent: GroupInheritanceDTO
    ): ResponseEntity<GroupMembershipDTO> {
        val added = groupService.addParent(parseUUID(id), parent.id!!, parent.validUntil)
        return ResponseEntity.ok(added.toDTO())
    }

    @PutMapping("/{id}/parents/{pid}")
    fun updateParent(
        @PathVariable id: String,
        @PathVariable pid: String,
        @Valid @RequestBody group: GroupMembershipMetadataDTO
    ): ResponseEntity<GroupMembershipDTO> {
        val updated = groupService.changeParent(parseUUID(id), parseUUID(pid), group.validUntil!!)
        return ResponseEntity.ok(updated.toDTO())
    }

    @DeleteMapping("/{id}/parents/{pid}")
    fun removeParent(
        @PathVariable id: String,
        @PathVariable pid: String
    ): ResponseEntity<Any> {
        groupService.removeParent(parseUUID(id), parseUUID(pid))
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/parents")
    fun listParents(
        @PathVariable id: String,
        @PageCursorParam cursor: PageCursorModel
    ): ResponseEntity<ListResponseDTO<GroupMembershipDTO>> {
        val res = groupService.listParents(parseUUID(id), cursor)
        return ResponseEntity.ok(ListResponseDTO(res.first.map { it.toDTO() }, res.second.toDTO()))
    }
}
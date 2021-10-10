package me.blvckbytes.authus.rest.controller

import me.blvckbytes.authus.rest.config.AControllerBase
import me.blvckbytes.authus.rest.config.PageCursorParam
import me.blvckbytes.authus.rest.dto.*
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import me.blvckbytes.authus.domain.service.PermissionService
import me.blvckbytes.authus.domain.service.UserAccountService
import me.blvckbytes.authus.domain.service.UserSessionService
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.validation.Valid

@RestController
@RequestMapping("/accounts")
class UserAccountController(
    @Autowired private var accountService: UserAccountService,
    @Autowired private var sessionService: UserSessionService,
    @Autowired private val userService: UserAccountService,
    @Autowired private val permissionService: PermissionService
) : AControllerBase(sessionService) {

    /////////////////////////////////////////////
    //             Stamp validator             //
    /////////////////////////////////////////////

    @GetMapping("{id}/is-stamp-latest/{stamp}")
    fun compareStamps(
        @PathVariable id: String,
        @PathVariable stamp: DateTime
    ): ResponseEntity<Any> {
        return booleanResponse(
            sessionService.isStampLatest(parseUUID(id), stamp)
        )
    }

    /////////////////////////////////////////////
    //              Account CRUD               //
    /////////////////////////////////////////////

    @GetMapping("/{id}")
    fun getAccount(
        @PathVariable id: String
    ): ResponseEntity<UserAccountDTO> {
        return ResponseEntity.ok(accountService.getAccount(parseUUID(id)).toDTO())
    }

    @PostMapping
    fun createAccount(
        @Valid @RequestBody account: UserAccountInputDTO
    ): ResponseEntity<UserAccountDTO> {
        val created = accountService.createAccount(account.toModel())
        return ResponseEntity.created(
            ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id)
                .toUri()
        ).body(created.toDTO())
    }

    @PutMapping("/{id}")
    fun changeAccount(
        @PathVariable id: String,
        @RequestBody @Valid account: UserAccountInputDTO
    ): ResponseEntity<UserAccountDTO> {
        return ResponseEntity.ok(accountService.updateAccount(parseUUID(id), account.toModel()).toDTO())
    }

    @DeleteMapping("/{id}")
    fun deleteAccount(
        @PathVariable id: String
    ): ResponseEntity<Any> {
        accountService.deleteAccount(parseUUID(id))
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun listAccounts(
        @PageCursorParam cursor: PageCursorModel
    ): ResponseEntity<ListResponseDTO<UserAccountDTO>> {
        val res = accountService.listAccounts(cursor)
        return ResponseEntity.ok(ListResponseDTO(res.first.map { it.toDTO() }, res.second.toDTO()))
    }

    /////////////////////////////////////////////
    //      Permission checking interface      //
    /////////////////////////////////////////////

    @GetMapping("/{id}/has-permission/{node}")
    fun checkIfHasPermission(
        @PathVariable id: String,
        @PathVariable node: String
    ): ResponseEntity<Any> {
        val permission = permissionService.getPermission(node)
        return booleanResponse(accountService.hasPermission(parseUUID(id), permission.id!!))
    }

    /////////////////////////////////////////////
    //      Explicit permission management     //
    /////////////////////////////////////////////

    @GetMapping("/{id}/permissions/{pid}")
    fun getExplicitPermission(
        @PathVariable id: String,
        @PathVariable pid: String,
    ): ResponseEntity<AssignedPermissionDTO> {
        return ResponseEntity.ok(accountService.getExplicitPermission(parseUUID(id), parseUUID(pid)).toDTO())
    }

    @PostMapping("/{id}/permissions")
    fun appendExplicitPermission(
       @PathVariable id: String,
       @Valid @RequestBody permission: AssignedPermissionDTO
    ): ResponseEntity<AssignedPermissionDTO> {
        val res = accountService.appendExplicitPermission(
            parseUUID(id), permission.id!!,
            permission.validUntil, permission.negative ?: false
        )
       return ResponseEntity.ok(res.toDTO())
    }

    @PutMapping("/{id}/permissions/{pid}")
    fun changeExplicitPermission(
        @PathVariable id: String,
        @PathVariable pid: String,
        @Valid @RequestBody permission: AssignedPermissionMetadataDTO
    ): ResponseEntity<AssignedPermissionDTO> {
        val res = accountService.updateExplicitPermission(
            parseUUID(id), parseUUID(pid),
            permission.validUntil, permission.negative ?: false
        )
        return ResponseEntity.ok(res.toDTO())
    }

    @DeleteMapping("/{id}/permissions/{pid}")
    fun deleteExplicitPermission(
        @PathVariable id: String,
        @PathVariable pid: String
    ): ResponseEntity<Any> {
        accountService.removeExplicitPermission(parseUUID(id), parseUUID(pid))
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/permissions")
    fun listExplicitPermissions(
        @PathVariable id: String,
        @PageCursorParam cursor: PageCursorModel
    ): ResponseEntity<ListResponseDTO<AssignedPermissionDTO>> {
        val res = accountService.listExplicitPermissions(parseUUID(id), cursor)
        return ResponseEntity.ok(ListResponseDTO(res.first.map { it.toDTO() }, res.second.toDTO()))
    }

    /////////////////////////////////////////////
    //       Group membership management       //
    /////////////////////////////////////////////

    @GetMapping("/{id}/in-group/{gid}")
    fun checkIfIsMember(
        @PathVariable id: String,
        @PathVariable gid: String
    ): ResponseEntity<Any> {
        return booleanResponse(userService.isInGroup(parseUUID(id), parseUUID(gid)))
    }

    @PostMapping("/{id}/groups")
    fun appendGroupMembership(
       @PathVariable id: String,
       @Valid @RequestBody group: GroupMembershipInputDTO
    ): ResponseEntity<GroupMembershipDTO> {
       val res = userService.createGroupMembership(
           parseUUID(id), group.id!!, group.validUntil
       )
       return ResponseEntity.ok(res.toDTO())
    }

    @PutMapping("/{id}/groups/{gid}")
    fun changeGroupMembership(
        @PathVariable id: String,
        @PathVariable gid: String,
        @Valid @RequestBody group: GroupMembershipMetadataDTO
    ): ResponseEntity<GroupMembershipDTO> {
        val res = userService.changeGroupMembership(
            parseUUID(id), parseUUID(gid), group.validUntil
        )
        return ResponseEntity.ok(res.toDTO())
    }

    @DeleteMapping("/{id}/groups/{gid}")
    fun deleteGroupMembership(
        @PathVariable id: String,
        @PathVariable gid: String,
    ): ResponseEntity<Any> {
        userService.removeGroupMembership(parseUUID(id), parseUUID(gid))
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/groups")
    fun listAssignedGroups(
        @PathVariable id: String,
        @PageCursorParam cursor: PageCursorModel,
    ): ResponseEntity<ListResponseDTO<GroupMembershipDTO>> {
        val res = accountService.listMemberships(parseUUID(id), cursor)
        return ResponseEntity.ok(ListResponseDTO(res.first.map { it.toDTO() }, res.second.toDTO()))
    }

}
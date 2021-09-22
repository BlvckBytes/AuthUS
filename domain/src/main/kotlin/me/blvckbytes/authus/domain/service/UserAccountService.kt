package me.blvckbytes.authus.domain.service

import me.blvckbytes.authus.domain.model.*
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import me.blvckbytes.authus.domain.repo.port.IUserAccountRepo
import me.blvckbytes.authus.domain.repo.port.IUserGroupsRepo
import me.blvckbytes.authus.domain.repo.port.IUserPermissionsRepo
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserAccountService(
    @Autowired private val accountRepo: IUserAccountRepo,
    @Autowired private val accountPermissionsRepo: IUserPermissionsRepo,
    @Autowired private val userGroupsRepo: IUserGroupsRepo
) {

    fun createAccount(account: UserAccountInputModel): UserAccountModel {
        return accountRepo.createAccount(account)
    }

    fun deleteAccount(id: UUID) {
        accountRepo.deleteAccount(id)
    }

    fun getAccount(id: UUID): UserAccountModel {
        return accountRepo.getAccount(id)
    }

    fun updateAccount(id: UUID, account: UserAccountInputModel): UserAccountModel {
        return accountRepo.updateAccount(id, account)
    }

    fun listAccounts(cursor: PageCursorModel): Pair<List<UserAccountModel>, PageCursorModel> {
        return accountRepo.listAccounts(cursor)
    }

    fun hasPermission(id: UUID, pid: UUID): Boolean {
        return accountPermissionsRepo.hasEffectivePermission(id, pid)
    }

    fun isInGroup(id: UUID, gid: UUID): Boolean {
        return userGroupsRepo.isInGroup(id, gid)
    }

    fun createGroupMembership(id: UUID, gid: UUID, validUntil: DateTime?): GroupMembershipModel {
        return userGroupsRepo.createGroupMembership(id, gid, validUntil)
    }

    fun changeGroupMembership(id: UUID, gid: UUID, validUntil: DateTime?): GroupMembershipModel {
        return userGroupsRepo.changeGroupMembership(id, gid, validUntil)
    }

    fun removeGroupMembership(id: UUID, gid: UUID) {
        return userGroupsRepo.removeGroupMembership(id, gid)
    }

    fun appendExplicitPermission(id: UUID, pid: UUID, validUntil: DateTime?, negative: Boolean): AssignedPermissionModel {
        return accountPermissionsRepo.appendExplicitPermission(id, pid, validUntil, negative)
    }

    fun updateExplicitPermission(id: UUID, pid: UUID, validUntil: DateTime?, negative: Boolean): AssignedPermissionModel {
        return accountPermissionsRepo.updateExplicitPermission(id, pid, validUntil, negative)
    }

    fun getExplicitPermission(aid: UUID, pid: UUID): AssignedPermissionModel {
        return accountPermissionsRepo.getExplicitPermission(aid, pid)
    }

    fun removeExplicitPermission(id: UUID, pid: UUID) {
        return accountPermissionsRepo.removeExplicitPermission(id, pid)
    }

    fun listExplicitPermissions(id: UUID, cursor: PageCursorModel): Pair<List<AssignedPermissionModel>, PageCursorModel> {
        return accountPermissionsRepo.listExplicitPermissions(id, cursor)
    }

    fun ensureExistence(id: UUID) {
        return accountRepo.ensureExistence(id)
    }

    fun listMemberships(id: UUID, cursor: PageCursorModel): Pair<List<GroupMembershipModel>, PageCursorModel> {
        return userGroupsRepo.listMemberships(id, cursor)
    }
}
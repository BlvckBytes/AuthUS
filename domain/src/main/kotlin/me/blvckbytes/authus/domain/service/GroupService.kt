package me.blvckbytes.authus.domain.service

import me.blvckbytes.authus.domain.model.AssignedPermissionModel
import me.blvckbytes.authus.domain.model.GroupMembershipModel
import me.blvckbytes.authus.domain.model.GroupModel
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import me.blvckbytes.authus.domain.repo.port.IGroupInheritanceRepo
import me.blvckbytes.authus.domain.repo.port.IGroupPermissionsRepo
import me.blvckbytes.authus.domain.repo.port.IGroupRepo
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class GroupService(
    @Autowired private val groupRepo: IGroupRepo,
    @Autowired private val groupPermissionsRepo: IGroupPermissionsRepo,
    @Autowired private val groupInheritanceRepo: IGroupInheritanceRepo
) {

    fun createGroup(group: GroupModel): GroupModel {
        return groupRepo.createGroup(group)
    }

    fun deleteGroup(id: UUID) {
        groupRepo.deleteGroup(id)
    }

    fun getGroup(id: UUID): GroupModel {
        return groupRepo.getGroup(id)
    }

    fun updateGroup(id: UUID, group: GroupModel): GroupModel {
        return groupRepo.updateGroup(id, group)
    }

    fun listGroups(cursor: PageCursorModel): Pair<List<GroupModel>, PageCursorModel> {
        return groupRepo.listGroups(cursor)
    }

    fun appendPermission(id: UUID, pid: UUID, validUntil: DateTime?, negative: Boolean): AssignedPermissionModel {
        return groupPermissionsRepo.appendPermission(id, pid, validUntil, negative)
    }

    fun updatePermission(id: UUID, pid: UUID, validUntil: DateTime?, negative: Boolean): AssignedPermissionModel {
        return groupPermissionsRepo.updatePermission(id, pid, validUntil, negative)
    }

    fun getPermission(gid: UUID, pid: UUID): AssignedPermissionModel {
        return groupPermissionsRepo.getPermission(gid, pid)
    }

    fun removePermission(id: UUID, pid: UUID) {
        groupPermissionsRepo.removePermission(id, pid)
    }

    fun listPermissions(id: UUID, cursor: PageCursorModel): Pair<List<AssignedPermissionModel>, PageCursorModel> {
        return groupPermissionsRepo.listPermissions(id, cursor)
    }

    fun addParent(id: UUID, pid: UUID, validUntil: DateTime?): GroupMembershipModel {
        return groupInheritanceRepo.addParent(id, pid, validUntil)
    }

    fun getParent(gid: UUID, pid: UUID): GroupMembershipModel {
        return groupInheritanceRepo.getParent(gid, pid)
    }

    fun changeParent(gid: UUID, pid: UUID, validUntil: DateTime): GroupMembershipModel {
        return groupInheritanceRepo.changeParent(gid, pid, validUntil)
    }

    fun listParents(gid: UUID, cursor: PageCursorModel): Pair<List<GroupMembershipModel>, PageCursorModel> {
        return groupInheritanceRepo.listParents(gid, cursor)
    }

    fun removeParent(id: UUID, pid: UUID) {
        return groupInheritanceRepo.removeParent(id, pid)
    }

    fun hasPermission(id: UUID, pid: UUID): Boolean {
        return groupPermissionsRepo.hasPermission(id, pid)
    }

    fun ensureExistence(id: UUID) {
        return groupRepo.ensureExistence(id)
    }
}
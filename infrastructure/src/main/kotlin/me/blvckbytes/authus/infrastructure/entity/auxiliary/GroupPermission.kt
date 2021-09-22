package me.blvckbytes.authus.infrastructure.entity.auxiliary

import me.blvckbytes.authus.domain.model.AssignedPermissionModel
import me.blvckbytes.authus.infrastructure.entity.Permission
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDEntityClass
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDModelEntity
import me.blvckbytes.authus.infrastructure.table.GroupsPermissions
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class GroupPermission(id: EntityID<UUID>): BaseUUIDModelEntity<AssignedPermissionModel>(id, GroupsPermissions) {
    companion object : BaseUUIDEntityClass<GroupPermission>(GroupsPermissions)

    var groupId by GroupsPermissions.groupId
    var permissionId by GroupsPermissions.permissionId
    var validUntil by GroupsPermissions.validUntil
    var negative by GroupsPermissions.negative

    private val permission by Permission referencedOn GroupsPermissions.permissionId

    override fun toModel(): AssignedPermissionModel {
        return AssignedPermissionModel(
            permission.toModel(), validUntil, negative
        )
    }
}
package me.blvckbytes.authus.infrastructure.entity.auxiliary

import me.blvckbytes.authus.domain.model.AssignedPermissionModel
import me.blvckbytes.authus.infrastructure.entity.Permission
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDEntityClass
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDModelEntity
import me.blvckbytes.authus.infrastructure.table.Permissions
import me.blvckbytes.authus.infrastructure.table.UsersPermissions
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class UserPermission(id: EntityID<UUID>): BaseUUIDModelEntity<AssignedPermissionModel>(id, UsersPermissions) {
    companion object : BaseUUIDEntityClass<UserPermission>(UsersPermissions)

    var userId by UsersPermissions.userId
    var permissionId by UsersPermissions.permissionId
    var validUntil by UsersPermissions.validUntil
    var negative by UsersPermissions.negative

    val permission by Permission referencedOn UsersPermissions.permissionId

    override fun toModel(): AssignedPermissionModel {
        return AssignedPermissionModel(
            permission.toModel(),
            validUntil, negative
        )
    }
}
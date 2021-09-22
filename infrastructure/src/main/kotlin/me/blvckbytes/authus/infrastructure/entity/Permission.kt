package me.blvckbytes.authus.infrastructure.entity

import me.blvckbytes.authus.domain.model.PermissionModel
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDEntityClass
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDModelEntity
import me.blvckbytes.authus.infrastructure.table.Permissions
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class Permission(id: EntityID<UUID>): BaseUUIDModelEntity<PermissionModel>(id, Permissions) {
    companion object : BaseUUIDEntityClass<Permission>(Permissions)

    var node by Permissions.node
    var description by Permissions.description

    override fun toModel(): PermissionModel {
        return PermissionModel(
            id.value, node, description
        )
    }
}
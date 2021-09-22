package me.blvckbytes.authus.infrastructure.repo

import me.blvckbytes.authus.domain.model.PermissionModel
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import me.blvckbytes.authus.domain.repo.port.IPermissionRepo
import me.blvckbytes.authus.infrastructure.entity.Permission
import me.blvckbytes.authus.infrastructure.table.GroupsPermissions
import me.blvckbytes.authus.infrastructure.table.Permissions
import me.blvckbytes.authus.infrastructure.table.UsersPermissions
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.util.*

@Repository
open class PermissionRepo : IPermissionRepo, ARepoBase<PermissionModel>("action_permission", Permissions) {

    override fun createPermission(permission: PermissionModel): PermissionModel {
        return transaction {

            // Permission with this node already exists
            if (Permissions.select { strColEq(Permissions.node, permission.node) }.count() > 0)
                throw collision("node", permission.node)

            Permission.new {
                node = permission.node
                description = permission.description
            }.toModel()
        }
    }

    override fun deletePermission(id: UUID) {
        transaction {
            // Check if this permission is used in a direct assignment
            if (UsersPermissions.select { UsersPermissions.permissionId eq id }.count() > 0)
                throw inUse(id)

            // Check if this permission is used in a group assignment
            if (GroupsPermissions.select { GroupsPermissions.permissionId eq id }.count() > 0)
                throw inUse(id)

            if (Permissions.deleteWhere { Permissions.id eq id } == 0)
                throw notFound(id)
        }
    }

    override fun getPermission(id: UUID): PermissionModel {
        return transaction {
            Permission
                .find { Permissions.id eq id }
                .firstOrNull()
                ?.toModel() ?: throw notFound(id)
        }
    }

    override fun getPermission(node: String): PermissionModel {
        return transaction {
            // Find permission by it's node value
            Permission
                .find { strColEq(Permissions.node, node) }
                .firstOrNull()
                ?.toModel() ?: throw notFound(node)
        }
    }

    override fun updatePermission(id: UUID, permission: PermissionModel): PermissionModel {
        return transaction {
            val target = Permission.find { Permissions.id eq id }.firstOrNull() ?: throw notFound(id)

            // The new node would collide with an existing name
            if (
                // Not the same node (change in place)
                !target.node.equals(permission.node, ignoreCase = true) &&

                // There already is a permission with the new node
                Permission.find { Permissions.node eq permission.node }.count() > 0
            )
                throw collision("node", permission.node)

            target.node = permission.node
            target.description = permission.description
            target.toModel()
        }
    }

    override fun listPermissions(cursor: PageCursorModel): Pair<List<PermissionModel>, PageCursorModel> {
        return transaction {
            applyCursor(Permissions.selectAll(), cursor, null, Permission) { it.toModel() }
        }
    }

    override fun ensureExistence(id: UUID) {
        transaction {
            if (Permission.find { Permissions.id eq id }.count() == 0L)
                throw notFound(id)
        }
    }
}
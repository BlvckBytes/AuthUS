package me.blvckbytes.authus.domain.service

import me.blvckbytes.authus.domain.model.PermissionModel
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import me.blvckbytes.authus.domain.repo.port.IPermissionRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class PermissionService(
    @Autowired private val permissionRepo: IPermissionRepo
) {

    fun createPermission(permission: PermissionModel): PermissionModel {
        return permissionRepo.createPermission(permission)
    }

    fun deletePermission(id: UUID) {
        return permissionRepo.deletePermission(id)
    }

    fun getPermission(id: UUID): PermissionModel {
        return permissionRepo.getPermission(id)
    }

    fun getPermission(node: String): PermissionModel {
        return permissionRepo.getPermission(node)
    }

    fun updatePermission(id: UUID, permission: PermissionModel): PermissionModel {
        return permissionRepo.updatePermission(id, permission)
    }

    fun listPermissions(cursor: PageCursorModel): Pair<List<PermissionModel>, PageCursorModel> {
        return permissionRepo.listPermissions(cursor)
    }

    fun ensureExistence(id: UUID) {
        return permissionRepo.ensureExistence(id)
    }
}
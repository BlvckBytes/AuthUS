package me.blvckbytes.authus.domain.repo.port

import me.blvckbytes.authus.domain.model.PermissionModel
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import java.util.*

/**
 * Responsible for creating, deleting, updating and fetching permissions
 */
interface IPermissionRepo {

    /**
     * Create a new permission
     * @param permission Values to save
     */
    fun createPermission(permission: PermissionModel): PermissionModel

    /**
     * Delete a permission by it's ID
     * @param node Node of target permission
     */
    fun deletePermission(id: UUID)

    /**
     * Get a permission by it's ID
     * @param id ID of the target permission
     */
    fun getPermission(id: UUID): PermissionModel


    /**
     * Get a permission by it's node
     * @param node Node of the target permission
     */
    fun getPermission(node: String): PermissionModel

    /**
     * Update a permission by it's ID
     * @param id ID of target permission
     * @param permission New state to update to
     */
    fun updatePermission(id: UUID, permission: PermissionModel): PermissionModel

    /**
     * List all available permissions
     * @param cursor Cursor containing search-information
     */
    fun listPermissions(cursor: PageCursorModel): Pair<List<PermissionModel>, PageCursorModel>

    /**
     * Ensure that the permission exists, raises an exception otherwise
     * @param id ID of target permission
     */
    fun ensureExistence(id: UUID)
}
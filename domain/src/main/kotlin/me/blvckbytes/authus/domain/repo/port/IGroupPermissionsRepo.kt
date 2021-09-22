package me.blvckbytes.authus.domain.repo.port

import me.blvckbytes.authus.domain.model.AssignedPermissionModel
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import org.joda.time.DateTime
import java.util.*

/**
 * Responsible for adding, removing, updating and fetching group permission
 * assignments. Also checks if a permission is assigned directly, or
 * if it's inherited through parents
 */
interface IGroupPermissionsRepo {

    /**
     * Append a permission to a group
     * @param gid ID of the target group
     * @param node Node of target permission
     * @param validUntil Expiration date of assignment
     * @param negative Whether or not this assignment is added or subtracted
     */
    fun appendPermission(gid: UUID, pid: UUID, validUntil: DateTime?, negative: Boolean): AssignedPermissionModel

    /**
     * Remove a permission from a group
     * @param gid ID of the target group
     * @param pid ID of target permission
     */
    fun removePermission(gid: UUID, pid: UUID)

    /**
     * Update an assigned permission within a group
     * @param gid ID of the target group
     * @param pid ID of target permission
     * @param validUntil Expiration date of assignment
     * @param negative Whether or not this assignment is added or subtracted
     */
    fun updatePermission(gid: UUID, pid: UUID, validUntil: DateTime?, negative: Boolean): AssignedPermissionModel

    /**
     * Get an assigned permission of a group
     * @param gid ID of the target group
     * @param pid ID of target permission
     */
    fun getPermission(gid: UUID, pid: UUID): AssignedPermissionModel

    /**
     * List all available permissions a group has
     * @param gid ID of the target group
     * @param cursor Cursor containing search-information
     */
    fun listPermissions(gid: UUID, cursor: PageCursorModel): Pair<List<AssignedPermissionModel>, PageCursorModel>


    /**
     * List all active permission nodes a group has, including
     * inherited nodes, accounting for negative permissions
     * @param gid ID of the target group
     */
    fun listActivePermissionNodes(gid: UUID): List<String>

    /**
     * Checks whether or not a group has a certain permission
     * @param gid ID of the target group
     * @param node Permission node in question
     */
    fun hasPermission(gid: UUID, pid: UUID): Boolean

    /**
     * Checks whether the group has this permission assigned directly (not over inheritance)
     * @param gid ID of the target group
     * @param pid ID of target permission
     * @param checkStamp True if it still has to be valid
     */
    fun hasPermissionDirectly(gid: UUID, pid: UUID, checkStamp: Boolean): Boolean

    /**
     * Ensure that the assignment exists, raises an exception otherwise
     * @param gid ID of the target group
     * @param pid ID of target permission
     */
    fun ensureExistence(gid: UUID, pid: UUID)
}
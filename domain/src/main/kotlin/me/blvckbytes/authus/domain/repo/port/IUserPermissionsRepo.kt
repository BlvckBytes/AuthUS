package me.blvckbytes.authus.domain.repo.port

import me.blvckbytes.authus.domain.model.AssignedPermissionModel
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import org.joda.time.DateTime
import java.util.*

/**
 * Responsible for creating, updating, removing and fetching explicit
 * user-permission assignments
 */
interface IUserPermissionsRepo {

    /**
     * Check whether or not a user has a certain permission due to
     * the group's assigned to this account
     * @param aid ID of the target account
     * @param node Permission-node of the target permission
     */
    fun hasEffectivePermission(aid: UUID, pid: UUID): Boolean

    /**
     * Append an explicit permission to an account
     * @param aid ID of the target account
     * @param pid ID of the target permission
     * @param validUntil Expiration date of permission
     * @param negative Whether or not this permission is added or retracted
     */
    fun appendExplicitPermission(aid: UUID, pid: UUID, validUntil: DateTime?, negative: Boolean): AssignedPermissionModel

    /**
     * Change an explicit permission assignment
     * @param aid ID of the target account
     * @param pid ID of the target permission
     * @param validUntil Expiration date of permission
     * @param negative Whether or not this permission is added or retracted
     */
    fun updateExplicitPermission(aid: UUID, pid: UUID, validUntil: DateTime?, negative: Boolean): AssignedPermissionModel

    /**
     * Get an assigned permission
     * @param aid ID of the target account
     * @param pid ID of the target permission
     */
    fun getExplicitPermission(aid: UUID, pid: UUID): AssignedPermissionModel

    /**
     * Remove an explicit permission from an account
     * @param aid ID of the target account
     * @param pid ID of the target permission
     */
    fun removeExplicitPermission(aid: UUID, pid: UUID)

    /**
     * List all explicit permissions of an account
     * @param aid ID of target account
     * @param cursor Page cursor
     */
    fun listExplicitPermissions(aid: UUID, cursor: PageCursorModel): Pair<List<AssignedPermissionModel>, PageCursorModel>

    /**
     * List all active explicit permission data, mapping node to negative-flag
     * @param aid ID of target account
     */
    fun listActiveExplicitPermissionData(aid: UUID): List<Pair<String, Boolean>>
}
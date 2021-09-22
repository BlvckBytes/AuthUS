package me.blvckbytes.authus.domain.repo.port

import me.blvckbytes.authus.domain.model.GroupMembershipModel
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import org.joda.time.DateTime
import java.util.*

/**
 * Responsible for adding, removing and listing parents of groups
 * Also lists parent- and sibling group IDs
 */
interface IGroupInheritanceRepo {

    /**
     * Add a new parent this group will inherit permissions from
     * @param gid ID of the target group
     * @param pid ID of the parent group
     * @param validUntil Expiration date of inheritance
     */
    fun addParent(gid: UUID, pid: UUID, validUntil: DateTime?): GroupMembershipModel

    /**
     * Remove a parent this group inherits from
     * @param gid ID of the target group
     * @param pid ID of the parent group
     */
    fun removeParent(gid: UUID, pid: UUID)

    /**
     * Get a parent group that the target inherits from
     * @param gid ID of the target group
     * @param pid ID of the parent group
     */
    fun getParent(gid: UUID, pid: UUID): GroupMembershipModel

    /**
     * List all parents a group inherits from
     * @param gid ID of the target group
     * @param cursor Cursor for pagination
     */
    fun listParents(gid: UUID, cursor: PageCursorModel): Pair<List<GroupMembershipModel>, PageCursorModel>

    /**
     * Change the parent metadata of a group
     * @param gid ID of the target group
     * @param pid ID of the target parent
     * @param validUntil Expiration date of inheritance
     */
    fun changeParent(gid: UUID, pid: UUID, validUntil: DateTime): GroupMembershipModel

    /**
     * Ensure that the inheritance exists, raises an exception otherwise
     * @param fid ID of the child group
     * @param pid ID of the parent group
     */
    fun ensureExistence(gid: UUID, pid: UUID)

    /**
     * Find all parent groups and return their IDs
     * @param gid ID of the target group
     */
    fun findParentGroupIds(gid: UUID): List<UUID>

    /**
     * Find all sibling groups and return their IDs
     * @param gid ID of the target group
     */
    fun findSiblingGroupIds(gid: UUID): List<UUID>
}
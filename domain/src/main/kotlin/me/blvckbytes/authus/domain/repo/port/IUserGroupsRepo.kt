package me.blvckbytes.authus.domain.repo.port

import me.blvckbytes.authus.domain.model.GroupMembershipModel
import me.blvckbytes.authus.domain.model.GroupModel
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import org.joda.time.DateTime
import java.util.*

/**
 * Responsible for creating, updating, removing and fetching
 * user-group memberships. Also provides a way to update all
 * account-hashes of group-members
 */
interface IUserGroupsRepo {

    /**
     * Check whether or not a user is member of a certain group
     * @param aid ID of the target account
     * @param gid ID of the target group
     */
    fun isInGroup(aid: UUID, gid: UUID): Boolean

    /**
     * Check whether or not a user is member of a certain group
     * @param aid ID of the target account
     * @param name Name of the target group
     */
    fun isInGroup(aid: UUID, name: String): Boolean

    /**
     * Assign a account to a certain group
     * @param aid ID of the target account
     * @param gid ID of the target group
     * @param validUntil Expiration date of assignment
     */
    fun createGroupMembership(aid: UUID, gid: UUID, validUntil: DateTime?): GroupMembershipModel

    /**
     * Change an existing group membership
     * @param aid ID of the target account
     * @param gid ID of the target group
     * @param validUntil Expiration date of assignment
     */
    fun changeGroupMembership(aid: UUID, gid: UUID, validUntil: DateTime?): GroupMembershipModel

    /**
     * Remove an account from a certain group
     * @param aid ID of the target account
     * @param gid ID of the target grou
     */
    fun removeGroupMembership(aid: UUID, gid: UUID)

    /**
     * List all memberships an account has
     * @param aid ID of the target account
     * @param cursor Page cursor
     */
    fun listMemberships(aid: UUID, cursor: PageCursorModel): Pair<List<GroupMembershipModel>, PageCursorModel>

    /**
     * List all group IDs an account is assigned to
     * @param aid ID of the target account
     */
    fun listMembershipGroupIds(aid: UUID): List<UUID>

    /**
     * Invokes a hash update for all members of the group
     * @param gid ID of the target group
     */
    fun memberAccountsHaveUpdated(gid: UUID)

    /**
     * Ensure that the account exists, raises an exception otherwise
     * @param aid ID of the target account
     * @param gid ID of the target group
     */
    fun ensureExistence(aid: UUID, gid: UUID)
}
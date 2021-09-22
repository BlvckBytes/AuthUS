package me.blvckbytes.authus.domain.repo.port

import me.blvckbytes.authus.domain.model.GroupModel
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import java.util.*

/**
 * Responsible for creating, deleting, updating and fetching groups
 */
interface IGroupRepo {

    /**
     * Create a new group
     * @param group Values to save
     */
    fun createGroup(group: GroupModel): GroupModel

    /**
     * Delete a group by it's ID
     * @param id ID of the target group
     */
    fun deleteGroup(id: UUID)

    /**
     * Get a group by it's ID
     * @param id ID of the target group
     */
    fun getGroup(id: UUID): GroupModel

    /**
     * Get a group by it's name
     * @param name Name of the target group
     */
    fun getGroup(name: String): GroupModel

    /**
     * Update a group by it's ID
     * @param id ID of the target group
     * @param group New state to update to
     */
    fun updateGroup(id: UUID, group: GroupModel): GroupModel

    /**
     * List all available groups
     * @param cursor Cursor containing search-information
     */
    fun listGroups(cursor: PageCursorModel): Pair<List<GroupModel>, PageCursorModel>

    /**
     * Ensure that the group exists, raises an exception otherwise
     * @param id ID of the group in question
     */
    fun ensureExistence(id: UUID)
}
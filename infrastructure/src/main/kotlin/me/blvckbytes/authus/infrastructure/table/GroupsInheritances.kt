package me.blvckbytes.authus.infrastructure.table

import org.jetbrains.exposed.sql.jodatime.datetime

/**
 * Links a group to it's parents
 */
object GroupsInheritances : BaseUUIDTable("groups_inheritances") {
    var groupId = reference("group_id", Groups)
    var parentGroupId = reference("parent_group_id", Groups)
    var validUntil = datetime("valid_until").nullable()

    init {
        // A group can only be inherited once, no matter if permanent or temporary
        index(true, groupId, parentGroupId)
    }
}
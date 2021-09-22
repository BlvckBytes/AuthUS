package me.blvckbytes.authus.infrastructure.table

import org.jetbrains.exposed.sql.jodatime.datetime

/**
 * Assigns a user to a group, with metadata
 */
object UsersGroups : BaseUUIDTable("users__groups") {
    var userId = reference("user_id", UserAccounts)
    var groupId = reference("group_id", Groups)
    var validUntil = datetime("valid_until").nullable()

    init {
        // A group can only be given once, no matter if permanent or temporary
        index(true, userId, groupId)
    }
}
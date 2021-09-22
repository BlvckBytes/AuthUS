package me.blvckbytes.authus.infrastructure.table

import org.jetbrains.exposed.sql.jodatime.datetime

/**
 * Assigns a permission to a group, with metadata
 */
object GroupsPermissions : BaseUUIDTable("groups__permissions") {
    var groupId = reference("group_id", Groups)
    var permissionId = reference("permission_id", Permissions.id)
    var validUntil = datetime("valid_until").nullable()
    var negative = bool("negative")

    init {
        // A permission can only be assigned once to a group, no matter if permanent or temporary
        index(true, groupId, permissionId)
    }
}
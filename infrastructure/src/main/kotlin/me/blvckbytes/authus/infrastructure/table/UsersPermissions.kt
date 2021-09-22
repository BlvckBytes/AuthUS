package me.blvckbytes.authus.infrastructure.table

import org.jetbrains.exposed.sql.jodatime.datetime

/**
 * Assigns a permission to a user, with metadata
 */
object UsersPermissions : BaseUUIDTable("users__permissions") {
    var userId = reference("user_id", UserAccounts)
    var permissionId = reference("permission_id", Permissions)
    var validUntil = datetime("valid_until").nullable()
    var negative = bool("negative")

    init {
        // A permission can only be given once, no matter if permanent or temporary
        index(true, userId, permissionId)
    }
}
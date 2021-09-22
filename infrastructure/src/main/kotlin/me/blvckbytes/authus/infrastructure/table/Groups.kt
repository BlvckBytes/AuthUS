package me.blvckbytes.authus.infrastructure.table

/**
 * Represents a group, which has permissions and members
 */
object Groups : BaseUUIDTable("groups") {
    var name = varchar("name", 255, "BINARY").uniqueIndex()
    var icon = varchar("icon", 255).nullable()
    var description = text("description").nullable()
}
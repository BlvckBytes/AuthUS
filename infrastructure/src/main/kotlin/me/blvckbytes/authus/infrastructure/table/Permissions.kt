package me.blvckbytes.authus.infrastructure.table

/**
 * Represents permissions which are required to perform corresponding actions
 */
object Permissions : BaseUUIDTable("permissions") {
    var node = varchar("node", 255, "BINARY").uniqueIndex()
    var description = text("description").nullable()
}
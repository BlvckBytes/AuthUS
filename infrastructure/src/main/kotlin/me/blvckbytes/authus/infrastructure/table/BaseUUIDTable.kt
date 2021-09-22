package me.blvckbytes.authus.infrastructure.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

/**
 * Every table should store the created-at and updated-at stamps
 */
abstract class BaseUUIDTable(name: String) : UUIDTable(name)  {
    // Created at and updated at are the basis of every table
    val createdAt = datetime("createdAt").clientDefault { currentUTC() }
    val updatedAt = datetime("updatedAt").nullable()

    // Helper function to get current UTC stamp
    fun currentUTC(): DateTime = DateTime.now(DateTimeZone.UTC)
}
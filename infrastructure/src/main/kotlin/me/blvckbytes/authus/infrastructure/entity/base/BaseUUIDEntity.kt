package me.blvckbytes.authus.infrastructure.entity.base

import me.blvckbytes.authus.infrastructure.table.BaseUUIDTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

/**
 * The base of every entity, containing created-at and updated-at stamps, ID based on UUIDs
 */
abstract class BaseUUIDEntity(id: EntityID<UUID>, table: BaseUUIDTable) : UUIDEntity(id) {
    val createdAt by table.createdAt
    var updatedAt by table.updatedAt
}
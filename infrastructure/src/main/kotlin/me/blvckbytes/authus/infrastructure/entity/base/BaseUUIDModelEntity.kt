package me.blvckbytes.authus.infrastructure.entity.base

import me.blvckbytes.authus.infrastructure.table.BaseUUIDTable
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

/**
 * Base of every entity that has the ability to convert to a domain-model
 */
abstract class BaseUUIDModelEntity<Model>(id: EntityID<UUID>, table: BaseUUIDTable) : BaseUUIDEntity(id, table) {
    abstract fun toModel(): Model
}
package me.blvckbytes.authus.infrastructure.entity.base

import me.blvckbytes.authus.infrastructure.table.BaseUUIDTable
import org.jetbrains.exposed.dao.EntityChangeType
import org.jetbrains.exposed.dao.EntityHook
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.toEntity

/**
 * Base of UUID entity-class which creates hook to change updated-at stamp event-based
 */
abstract class BaseUUIDEntityClass<Entity: BaseUUIDEntity>(table: BaseUUIDTable): UUIDEntityClass<Entity>(table) {

    init {
        EntityHook.subscribe {
            if (it.changeType == EntityChangeType.Updated)
                it.toEntity(this)?.updatedAt = table.currentUTC()
        }
    }
}
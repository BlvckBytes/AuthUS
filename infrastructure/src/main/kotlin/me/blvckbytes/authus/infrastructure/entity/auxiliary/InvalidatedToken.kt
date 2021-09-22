package me.blvckbytes.authus.infrastructure.entity.auxiliary

import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDEntity
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDEntityClass
import me.blvckbytes.authus.infrastructure.table.InvalidatedTokens
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class InvalidatedToken(id: EntityID<UUID>): BaseUUIDEntity(id, InvalidatedTokens)  {
    companion object : BaseUUIDEntityClass<InvalidatedToken>(InvalidatedTokens)

    var tokenId by InvalidatedTokens.tokenId
    var validUntil by InvalidatedTokens.validUntil
}
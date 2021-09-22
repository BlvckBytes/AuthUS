package me.blvckbytes.authus.infrastructure.table

import org.jetbrains.exposed.sql.jodatime.datetime

object InvalidatedTokens : BaseUUIDTable("invalidated_tokens") {
    var tokenId = uuid("token_id").uniqueIndex()
    var validUntil = datetime("valid_until")
}
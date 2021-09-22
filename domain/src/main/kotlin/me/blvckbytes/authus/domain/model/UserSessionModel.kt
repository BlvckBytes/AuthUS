package me.blvckbytes.authus.domain.model

import me.blvckbytes.authus.domain.exception.NoPermissionException
import org.joda.time.DateTime
import java.util.*

class UserSessionModel(
    val id: UUID,
    val issuedAt: DateTime,
    val validUntil: DateTime,
    val holderAccountId: UUID,
    val activePermissionNodes: List<String>,
    val lastAccountUpdate: DateTime,
    val accessToken: String,
    val refreshToken: String?
) {
    fun ensurePermission(node: String) {
        if(!activePermissionNodes.map { it.lowercase() }.contains(node.lowercase()))
            throw NoPermissionException(node)
    }
}
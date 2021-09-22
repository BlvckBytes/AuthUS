package me.blvckbytes.authus.application.dto

import com.fasterxml.jackson.annotation.JsonInclude
import me.blvckbytes.authus.domain.model.UserSessionModel
import org.joda.time.DateTime
import java.util.*

class UserSessionDTO (
    val issued_at: DateTime?,
    val valid_until: DateTime?,
    val holder_account_id: UUID?,
    val active_permission_nodes: List<String>?,
    val lastAccountUpdate: DateTime?,
    val access_token: String?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val refresh_token: String?
)

fun UserSessionModel.toDTO(): UserSessionDTO {
    return UserSessionDTO(issuedAt, validUntil, holderAccountId, activePermissionNodes, lastAccountUpdate, accessToken, refreshToken)
}
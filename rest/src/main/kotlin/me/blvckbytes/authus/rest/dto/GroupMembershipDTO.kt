package me.blvckbytes.authus.rest.dto

import me.blvckbytes.authus.domain.model.GroupMembershipModel
import org.joda.time.DateTime
import java.util.*

class GroupMembershipDTO(
    var id: UUID?,
    val name: String?,
    val description: String?,
    val validUntil: DateTime?
)

fun GroupMembershipModel.toDTO(): GroupMembershipDTO {
    return GroupMembershipDTO(group.id, group.name, group.description, validUntil)
}
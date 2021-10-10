package me.blvckbytes.authus.rest.dto

import me.blvckbytes.authus.domain.model.AssignedPermissionModel
import org.joda.time.DateTime
import java.util.*
import javax.validation.constraints.NotNull

class AssignedPermissionDTO(
    @field:NotNull
    var id: UUID?,

    var node: String?,
    var description: String?,

    var validUntil: DateTime?,
    var negative: Boolean?
)

fun AssignedPermissionModel.toDTO(): AssignedPermissionDTO {
    return AssignedPermissionDTO(
        permission.id, permission.node, permission.description,
        validUntil, negative
    )
}
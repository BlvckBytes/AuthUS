package me.blvckbytes.authus.application.dto

import me.blvckbytes.authus.domain.model.PermissionModel
import java.util.*
import javax.validation.constraints.NotNull

open class PermissionDTO(
    var id: UUID?,

    @field:NotNull
    var node: String?,

    var description: String?
) {
    fun toModel(): PermissionModel {
        return PermissionModel(null, node!!, description)
    }
}

fun PermissionModel.toDTO(): PermissionDTO {
    return PermissionDTO(id, node, description)
}
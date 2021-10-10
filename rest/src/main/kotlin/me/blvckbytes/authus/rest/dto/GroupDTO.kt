package me.blvckbytes.authus.rest.dto

import com.fasterxml.jackson.annotation.JsonInclude
import me.blvckbytes.authus.domain.model.GroupModel
import java.util.*
import javax.validation.constraints.NotNull

class GroupDTO(
    var id: UUID?,

    @field:NotNull
    val name: String?,

    val icon: String?,

    val description: String?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val parents: List<GroupMembershipDTO>?
) {
    fun toModel(): GroupModel {
        return GroupModel(id, name!!, icon, description, emptyList())
    }
}

fun GroupModel.toDTO(): GroupDTO {
    return GroupDTO(id, name, icon, description, parents?.map { it.toDTO() })
}
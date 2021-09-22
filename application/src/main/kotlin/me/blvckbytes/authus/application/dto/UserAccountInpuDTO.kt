package me.blvckbytes.authus.application.dto

import com.fasterxml.jackson.annotation.JsonInclude
import me.blvckbytes.authus.domain.model.UserAccountInputModel
import javax.validation.constraints.NotNull

class UserAccountInputDTO(
    @field:NotNull
    var username: String?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @field:NotNull
    var password: String?,

    @field:NotNull
    var email: String?
) {
    fun toModel(): UserAccountInputModel {
        return UserAccountInputModel(username!!, password!!, email!!)
    }
}

fun UserAccountInputModel.toDTO(): UserAccountInputDTO {
    return UserAccountInputDTO(username, password, email)
}
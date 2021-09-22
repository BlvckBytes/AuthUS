package me.blvckbytes.authus.application.dto

import me.blvckbytes.authus.domain.model.UserCredentialsModel
import javax.validation.constraints.NotNull

class UserCredentialsDTO (

    @field:NotNull
    val username: String?,

    @field:NotNull
    val password: String?
) {
    fun toModel(): UserCredentialsModel {
        return UserCredentialsModel(username!!, password!!)
    }
}
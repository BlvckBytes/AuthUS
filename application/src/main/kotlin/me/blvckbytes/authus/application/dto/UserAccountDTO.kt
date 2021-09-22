package me.blvckbytes.authus.application.dto

import com.fasterxml.jackson.annotation.JsonInclude
import me.blvckbytes.authus.domain.model.UserAccountModel
import org.joda.time.DateTime
import java.util.*
import javax.validation.constraints.NotNull

class UserAccountDTO(
    var id: UUID?,

    @field:NotNull
    var username: String?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @field:NotNull
    var password: String?,

    @field:NotNull
    var email: String?,

    var joined: DateTime?
)

fun UserAccountModel.toDTO(): UserAccountDTO {
    return UserAccountDTO(id, username, null, email, joined)
}
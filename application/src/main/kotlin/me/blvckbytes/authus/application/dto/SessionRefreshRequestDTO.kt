package me.blvckbytes.authus.application.dto

import javax.validation.constraints.NotNull

class SessionRefreshRequestDTO(
    @field:NotNull
    var refresh_token: String?
)
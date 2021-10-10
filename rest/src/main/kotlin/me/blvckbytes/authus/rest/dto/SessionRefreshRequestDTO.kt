package me.blvckbytes.authus.rest.dto

import javax.validation.constraints.NotNull

class SessionRefreshRequestDTO(
    @field:NotNull
    var refresh_token: String?
)
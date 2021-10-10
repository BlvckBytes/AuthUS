package me.blvckbytes.authus.rest.dto

import org.joda.time.DateTime
import java.util.*
import javax.validation.constraints.NotNull

class GroupMembershipInputDTO(
    @field:NotNull
    val id: UUID?,
    val validUntil: DateTime?
)
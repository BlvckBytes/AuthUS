package me.blvckbytes.authus.application.dto

import org.joda.time.DateTime
import javax.validation.constraints.NotNull

class GroupMembershipMetadataDTO(
    @field:NotNull
    val validUntil: DateTime?
)
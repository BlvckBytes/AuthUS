package me.blvckbytes.authus.rest.dto

import org.joda.time.DateTime
import java.util.*
import javax.validation.constraints.NotNull

class GroupInheritanceDTO(
    @field:NotNull
    var id: UUID?,

    var validUntil: DateTime?
)
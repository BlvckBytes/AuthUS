package me.blvckbytes.authus.application.dto

import org.joda.time.DateTime

class AssignedPermissionMetadataDTO(
    var validUntil: DateTime?,
    var negative: Boolean?
)
package me.blvckbytes.authus.rest.dto

import org.joda.time.DateTime

class AssignedPermissionMetadataDTO(
    var validUntil: DateTime?,
    var negative: Boolean?
)
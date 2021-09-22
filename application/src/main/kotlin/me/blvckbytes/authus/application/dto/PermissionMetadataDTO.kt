package me.blvckbytes.authus.application.dto

import org.joda.time.DateTime

class PermissionMetadataDTO(
    var validUntil: DateTime?,
    var negative: Boolean?
)
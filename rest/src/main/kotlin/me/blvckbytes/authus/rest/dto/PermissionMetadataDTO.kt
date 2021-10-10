package me.blvckbytes.authus.rest.dto

import org.joda.time.DateTime

class PermissionMetadataDTO(
    var validUntil: DateTime?,
    var negative: Boolean?
)
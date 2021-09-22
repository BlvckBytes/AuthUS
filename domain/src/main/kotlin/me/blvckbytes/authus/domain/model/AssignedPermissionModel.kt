package me.blvckbytes.authus.domain.model

import org.joda.time.DateTime

class AssignedPermissionModel(
    var permission: PermissionModel,
    var validUntil: DateTime?,
    var negative: Boolean
)
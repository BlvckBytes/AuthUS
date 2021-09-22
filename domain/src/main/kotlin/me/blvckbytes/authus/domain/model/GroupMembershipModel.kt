package me.blvckbytes.authus.domain.model

import org.joda.time.DateTime

class GroupMembershipModel(
    var group: GroupModel,
    var validUntil: DateTime?
)
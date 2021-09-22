package me.blvckbytes.authus.domain.model

import java.util.*

class GroupModel(
    var id: UUID?,
    val name: String,
    val icon: String?,
    val description: String?,
    val parents: List<GroupMembershipModel>?
)
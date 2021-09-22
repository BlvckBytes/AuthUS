package me.blvckbytes.authus.domain.model

import java.util.*

open class PermissionModel(
    val id: UUID?,
    val node: String,
    val description: String?
)
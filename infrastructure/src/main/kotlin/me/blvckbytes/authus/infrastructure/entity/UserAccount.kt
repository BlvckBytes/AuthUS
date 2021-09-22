package me.blvckbytes.authus.infrastructure.entity

import me.blvckbytes.authus.domain.model.UserAccountModel
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDEntityClass
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDModelEntity
import me.blvckbytes.authus.infrastructure.table.UserAccounts
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import java.util.*

class UserAccount(id: EntityID<UUID>): BaseUUIDModelEntity<UserAccountModel>(id, UserAccounts) {
    companion object : BaseUUIDEntityClass<UserAccount>(UserAccounts)

    var username by UserAccounts.username
    var passHash by UserAccounts.passHash
    var email by UserAccounts.email

    override fun toModel(): UserAccountModel {
        return UserAccountModel(
            this.id.value, this.username, passHash, email, createdAt, updatedAt ?: DateTime.now()
        )
    }
}
package me.blvckbytes.authus.infrastructure.entity.auxiliary

import me.blvckbytes.authus.domain.model.GroupMembershipModel
import me.blvckbytes.authus.infrastructure.entity.Group
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDEntityClass
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDModelEntity
import me.blvckbytes.authus.infrastructure.table.UsersGroups
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class UserGroup(id: EntityID<UUID>): BaseUUIDModelEntity<GroupMembershipModel>(id, UsersGroups) {
    companion object : BaseUUIDEntityClass<UserGroup>(UsersGroups)

    var userId by UsersGroups.userId
    var groupId by UsersGroups.groupId
    var validUntil by UsersGroups.validUntil

    private val group by Group referencedOn UsersGroups.groupId

    override fun toModel(): GroupMembershipModel {
        return GroupMembershipModel(
            group.toModel(), validUntil
        )
    }
}
package me.blvckbytes.authus.infrastructure.entity.auxiliary

import me.blvckbytes.authus.domain.model.GroupMembershipModel
import me.blvckbytes.authus.domain.model.GroupModel
import me.blvckbytes.authus.infrastructure.entity.Group
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDEntityClass
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDModelEntity
import me.blvckbytes.authus.infrastructure.table.GroupsInheritances
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class GroupInheritance(id: EntityID<UUID>): BaseUUIDModelEntity<GroupMembershipModel>(id, GroupsInheritances) {
    companion object : BaseUUIDEntityClass<GroupInheritance>(GroupsInheritances)

    var groupId by GroupsInheritances.groupId
    var parentGroupId by GroupsInheritances.parentGroupId
    var validUntil by GroupsInheritances.validUntil

    val parentGroup by Group referencedOn GroupsInheritances.parentGroupId

    override fun toModel(): GroupMembershipModel {
        return GroupMembershipModel(
            GroupModel(
                parentGroup.id.value, parentGroup.name, parentGroup.description, parentGroup.icon,
                null // Group inheritance is only resolved one level deep
            ),
            validUntil
        )
    }
}
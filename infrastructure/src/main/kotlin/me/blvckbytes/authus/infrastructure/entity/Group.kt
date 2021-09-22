package me.blvckbytes.authus.infrastructure.entity

import me.blvckbytes.authus.domain.model.GroupMembershipModel
import me.blvckbytes.authus.domain.model.GroupModel
import me.blvckbytes.authus.infrastructure.entity.auxiliary.GroupInheritance
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDEntityClass
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDModelEntity
import me.blvckbytes.authus.infrastructure.table.Groups
import me.blvckbytes.authus.infrastructure.table.GroupsInheritances
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class Group(id: EntityID<UUID>): BaseUUIDModelEntity<GroupModel>(id, Groups) {
    companion object : BaseUUIDEntityClass<Group>(Groups)

    var name by Groups.name
    var icon by Groups.icon
    var description by Groups.description

    private val parents by GroupInheritance referrersOn GroupsInheritances.groupId

    override fun toModel(): GroupModel {
        return GroupModel(
            id.value, name, icon, description, parents.map {
                GroupMembershipModel(
                    GroupModel(
                        it.parentGroup.id.value, it.parentGroup.name,
                        it.parentGroup.description, it.parentGroup.icon,
                        null // Parents are only resolved one level deep
                    ),
                    it.validUntil
                )
            }
        )
    }
}
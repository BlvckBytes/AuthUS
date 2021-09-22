package me.blvckbytes.authus.infrastructure.repo

import me.blvckbytes.authus.domain.model.GroupModel
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import me.blvckbytes.authus.domain.repo.port.IGroupRepo
import me.blvckbytes.authus.infrastructure.entity.Group
import me.blvckbytes.authus.infrastructure.table.Groups
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.util.*

@Repository
open class GroupRepo : IGroupRepo, ARepoBase<GroupModel>("permission_group", Groups) {

    override fun createGroup(group: GroupModel): GroupModel {
        return transaction {

            // Group with this name already exists
            if (Groups.select { strColEq(Groups.name, group.name) }.count() > 0)
                throw collision("name", group.name)

            Group.new {
                name = group.name
                description = group.description
                icon = group.icon
            }.toModel()
        }
    }

    override fun deleteGroup(id: UUID) {
        transaction {
            if (Groups.deleteWhere { Groups.id eq id } == 0)
                throw notFound(id)
        }
    }

    override fun getGroup(id: UUID): GroupModel {
        return transaction {
            Group.findById(id)?.toModel() ?: throw notFound(id)
        }
    }

    override fun getGroup(name: String): GroupModel {
        return transaction {
            // Find group by it's binary varchar value
            Group
                .find { strColEq(Groups.name, name) }
                .firstOrNull()
                ?.toModel() ?: throw notFound(name)
        }
    }

    override fun updateGroup(id: UUID, group: GroupModel): GroupModel {
        return transaction {

            // The new name would collide with an existing name
            if (
                Groups.select {
                    strColEq(Groups.name, group.name) and
                    (Groups.id neq id)
                }.count() > 0
            )
                throw collision("name", group.name)

            val target = Group.findById(id) ?: throw notFound(id)
            target.name = group.name
            target.description = group.description
            target.icon = group.icon
            target.toModel()
        }
    }

    override fun listGroups(cursor: PageCursorModel): Pair<List<GroupModel>, PageCursorModel> {
        return transaction {
            applyCursor(Groups.selectAll(), cursor, null, Group) { it.toModel() }
        }
    }

    override fun ensureExistence(id: UUID) {
         transaction {
            if (Groups.select { Groups.id eq id }.count() == 0L)
                throw notFound(id)
        }
    }
}
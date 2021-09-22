package me.blvckbytes.authus.infrastructure.repo

import me.blvckbytes.authus.domain.exception.ModelCollisionException
import me.blvckbytes.authus.domain.model.GroupMembershipModel
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import me.blvckbytes.authus.domain.repo.port.IGroupInheritanceRepo
import me.blvckbytes.authus.domain.repo.port.IGroupRepo
import me.blvckbytes.authus.domain.repo.port.IUserGroupsRepo
import me.blvckbytes.authus.infrastructure.entity.auxiliary.GroupInheritance
import me.blvckbytes.authus.infrastructure.table.Groups
import me.blvckbytes.authus.infrastructure.table.GroupsInheritances
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import java.util.*

@Repository
open class GroupInheritanceRepo(
    @Autowired @Lazy private val groupRepo: IGroupRepo,
    @Autowired @Lazy private val userGroupsRepo: IUserGroupsRepo
) : ARepoBase<GroupMembershipModel>("group_inheritance", GroupsInheritances), IGroupInheritanceRepo {

    override fun addParent(gid: UUID, pid: UUID, validUntil: DateTime?): GroupMembershipModel {
        return transaction {
            // This group already directly inherits from the stated group
            if (
                GroupsInheritances.select {
                    (GroupsInheritances.groupId eq gid) and
                    (GroupsInheritances.parentGroupId eq pid)
                }.count() > 0
            )
                throw ModelCollisionException("group_inheritance", "(gid, pid)", "($gid, ${pid})")

            // The parents of this requested parent already contain this child-ID, which would cause a loop
            if (findParentGroupIds(pid).contains(gid))
                throw ModelCollisionException("inheritance_tree", "(gid, pid)", "($pid, ${gid})")

            // Group has to exist
            groupRepo.ensureExistence(gid)

            // Parent group has to exist
            groupRepo.ensureExistence(pid)

            val created = GroupInheritance.new {
                groupId = EntityID(gid, Groups)
                parentGroupId = EntityID(pid, Groups)
                this.validUntil = validUntil
            }

            // Update members of the group that just inherited and all it's siblings
            listOf(gid).plus(findSiblingGroupIds(gid)).forEach {
                userGroupsRepo.memberAccountsHaveUpdated(it)
            }

            created.toModel()
        }
    }

    override fun removeParent(gid: UUID, pid: UUID) {
        transaction {
            // This group doesn't inherit from the stated group
            if (
                GroupsInheritances.deleteWhere {
                    (GroupsInheritances.groupId eq gid) and
                    (GroupsInheritances.parentGroupId eq pid)
                } == 0
            )
                throw notFound("($gid, $pid)")

            // Update members of the group that just inherited and all it's siblings
            listOf(gid).plus(findSiblingGroupIds(gid)).forEach {
                userGroupsRepo.memberAccountsHaveUpdated(it)
            }
        }
    }

    override fun getParent(gid: UUID, pid: UUID): GroupMembershipModel {
        return transaction {
            GroupInheritance.find {
                (GroupsInheritances.groupId eq gid) and
                (GroupsInheritances.parentGroupId eq pid)
            }.firstOrNull()?.toModel() ?: throw notFound("($gid, $pid)")
        }
    }

    override fun listParents(gid: UUID, cursor: PageCursorModel): Pair<List<GroupMembershipModel>, PageCursorModel> {
        return transaction {
            applyCursor(
                GroupsInheritances.innerJoin(Groups).selectAll(),
                cursor,
                Op.build { GroupsInheritances.groupId eq gid },
                GroupInheritance
            ) { it.toModel() }
        }
    }

    override fun changeParent(gid: UUID, pid: UUID, validUntil: DateTime): GroupMembershipModel {
        return transaction {
            val target = GroupInheritance.find {
                (GroupsInheritances.groupId eq gid) and
                (GroupsInheritances.parentGroupId eq pid)
            }.firstOrNull() ?: throw notFound("($gid, $pid)")

            target.validUntil = validUntil
            target.toModel()
        }
    }

    override fun ensureExistence(gid: UUID, pid: UUID) {
        transaction {
            val qry = GroupsInheritances.select {
                (GroupsInheritances.groupId eq gid) and
                (GroupsInheritances.parentGroupId eq pid)
            }

            if (qry.count() == 0L)
                throw notFound("($gid, $pid)")
        }
    }

    override fun findParentGroupIds(gid: UUID): List<UUID> {
        // Find all inherited group IDs
        val groupIds = GroupsInheritances
            .slice(GroupsInheritances.parentGroupId)
            .select {
                (GroupsInheritances.groupId eq gid) and
                (GroupsInheritances.validUntil.greater(DateTime.now()))
            }.map { it[GroupsInheritances.parentGroupId].value }

        // For these inherited groups, find their parents again recursively
        val resulting = groupIds.toMutableList()
        groupIds.forEach { resulting.addAll(findParentGroupIds(it)) }
        return resulting
    }

    override fun findSiblingGroupIds(gid: UUID): List<UUID> {
        // Find all group IDs where the parent is this group (=siblings)
        val groupIds = GroupsInheritances
            .slice(GroupsInheritances.parentGroupId)
            .select {
                (GroupsInheritances.parentGroupId eq gid) and
                (GroupsInheritances.validUntil.greater(DateTime.now()))
            }.map { it[GroupsInheritances.groupId].value }

        // For these sibling groups, find their siblings again recursively
        val resulting = groupIds.toMutableList()
        groupIds.forEach { resulting.addAll(findSiblingGroupIds(it)) }
        return resulting
    }
}
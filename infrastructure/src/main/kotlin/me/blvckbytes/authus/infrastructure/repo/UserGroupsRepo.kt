package me.blvckbytes.authus.infrastructure.repo

import me.blvckbytes.authus.domain.exception.ModelCollisionException
import me.blvckbytes.authus.domain.exception.ModelNotFoundException
import me.blvckbytes.authus.domain.model.GroupMembershipModel
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import me.blvckbytes.authus.domain.repo.port.IGroupRepo
import me.blvckbytes.authus.domain.repo.port.IUserAccountRepo
import me.blvckbytes.authus.domain.repo.port.IUserGroupsRepo
import me.blvckbytes.authus.infrastructure.entity.auxiliary.UserGroup
import me.blvckbytes.authus.infrastructure.table.Groups
import me.blvckbytes.authus.infrastructure.table.UserAccounts
import me.blvckbytes.authus.infrastructure.table.UsersGroups
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import java.util.*

@Repository
open class UserGroupsRepo(
    @Autowired @Lazy private val accountRepo: IUserAccountRepo,
    @Autowired @Lazy private val groupRepo: IGroupRepo
) : ARepoBase<GroupMembershipModel>("group_membership", UsersGroups), IUserGroupsRepo {

    override fun memberAccountsHaveUpdated(gid: UUID) {
        transaction {
            // Update the account hash for all members of this group
            UsersGroups
                .slice(UsersGroups.userId)
                .select { UsersGroups.groupId eq gid }
                .map { it[UsersGroups.userId].value }
                .forEach { accountRepo.accountHasUpdated(it) }
        }
    }

    override fun isInGroup(aid: UUID, gid: UUID): Boolean {
        return transaction {
            UsersGroups.select {
                (UsersGroups.userId eq aid) and
                (UsersGroups.groupId eq gid) and
                (UsersGroups.validUntil.greater(DateTime.now()) or UsersGroups.validUntil.isNull())
            }.count() > 0
        }
    }

    override fun isInGroup(aid: UUID, name: String): Boolean {
        return isInGroup(aid, groupRepo.getGroup(name).id!!)
    }

    override fun createGroupMembership(aid: UUID, gid: UUID, validUntil: DateTime?): GroupMembershipModel {
        return transaction {
            if (isInGroup(aid, gid))
                throw ModelCollisionException("group_membership", "(id, gid)", "($aid, $gid)")

            val created = UserGroup.new {
                userId = EntityID(aid, UserAccounts)
                groupId = EntityID(gid, Groups)
                this.validUntil = validUntil
            }.toModel()

            accountRepo.accountHasUpdated(aid)
            created
        }
    }

    override fun changeGroupMembership(aid: UUID, gid: UUID, validUntil: DateTime?): GroupMembershipModel {
        return transaction {
            val target = UserGroup.find {
                (UsersGroups.userId eq aid) and
                (UsersGroups.groupId eq gid)
            }.firstOrNull() ?: throw ModelNotFoundException("group_membership", "($aid, $gid)")

            target.validUntil = validUntil

            accountRepo.accountHasUpdated(aid)
            target.toModel()
        }
    }

    override fun removeGroupMembership(aid: UUID, gid: UUID) {
        return transaction {
            if (
                UsersGroups.deleteWhere {
                    (UsersGroups.userId eq aid) and
                    (UsersGroups.groupId eq gid)
                } == 0
            )
                throw ModelNotFoundException("group_membership", "($aid, $gid)")

            accountRepo.accountHasUpdated(aid)
        }
    }

    override fun listMemberships(aid: UUID, cursor: PageCursorModel): Pair<List<GroupMembershipModel>, PageCursorModel> {
        return transaction {
            applyCursor(
                UsersGroups.innerJoin(Groups).selectAll(),
                cursor,
                Op.build {
                    // Entries regarding target user
                    (UsersGroups.userId eq aid) and

                    // Retrieve only still active memberships
                    (UsersGroups.validUntil.isNull() or UsersGroups.validUntil.greater(DateTime.now()))
                },
                UserGroup
            ) { it.toModel() }
        }
    }

    override fun listMembershipGroupIds(aid: UUID): List<UUID> {
        return transaction {
            UsersGroups.select {
                (UsersGroups.userId eq aid)
            }.map { it[UsersGroups.groupId].value }
        }
    }

    override fun ensureExistence(aid: UUID, gid: UUID) {
        transaction {
            val qry = UsersGroups.select {
                (UsersGroups.userId eq aid) and
                (UsersGroups.groupId eq gid)
            }

            if (qry.count() == 0L)
                throw notFound("($aid, $gid)")
        }
    }
}
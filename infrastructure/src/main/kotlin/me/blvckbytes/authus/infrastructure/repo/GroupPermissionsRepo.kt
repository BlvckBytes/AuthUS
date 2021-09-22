package me.blvckbytes.authus.infrastructure.repo

import me.blvckbytes.authus.domain.exception.ModelCollisionException
import me.blvckbytes.authus.domain.model.AssignedPermissionModel
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import me.blvckbytes.authus.domain.repo.port.*
import me.blvckbytes.authus.infrastructure.entity.auxiliary.GroupPermission
import me.blvckbytes.authus.infrastructure.table.Groups
import me.blvckbytes.authus.infrastructure.table.GroupsPermissions
import me.blvckbytes.authus.infrastructure.table.Permissions
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import java.util.*

@Repository
open class GroupPermissionsRepo(
    @Autowired @Lazy private val groupRepo: IGroupRepo,
    @Autowired @Lazy private val permissionRepo: IPermissionRepo,
    @Autowired @Lazy private val userGroupsRepo: IUserGroupsRepo,
    @Autowired @Lazy private val groupInheritanceRepo: IGroupInheritanceRepo
) : ARepoBase<AssignedPermissionModel>("permission_assignment", GroupsPermissions), IGroupPermissionsRepo {

    override fun appendPermission(
        gid: UUID,
        pid: UUID,
        validUntil: DateTime?,
        negative: Boolean
    ): AssignedPermissionModel {
        return transaction {
            permissionRepo.ensureExistence(pid)

            // Permission is already directly assigned
            if (hasPermissionDirectly(gid, pid, false))
                throw ModelCollisionException("permission_assignment", "(gid, pid)", "($id, $pid)")

            // Group has to exist
            groupRepo.ensureExistence(gid)

            // Permission has to exist
            permissionRepo.ensureExistence(pid)

            // Create permission assignment
            val created = GroupPermission.new {
                groupId = EntityID(gid, Groups)
                permissionId = EntityID(pid, Permissions)
                this.validUntil = validUntil
                this.negative = negative
            }.toModel()

            userGroupsRepo.memberAccountsHaveUpdated(gid)
            created
        }
    }

    override fun removePermission(gid: UUID, pid: UUID) {
        transaction {
            permissionRepo.ensureExistence(pid)

            // Delete based on primary key
            if (
                GroupsPermissions.deleteWhere {
                    (GroupsPermissions.groupId eq gid) and
                    (GroupsPermissions.permissionId eq pid)
                } == 0
            )
                throw notFound("($gid, $pid)")

            userGroupsRepo.memberAccountsHaveUpdated(gid)
        }
    }

    override fun updatePermission(
        gid: UUID,
        pid: UUID,
        validUntil: DateTime?,
        negative: Boolean
    ): AssignedPermissionModel {
        return transaction {
            permissionRepo.ensureExistence(pid)

            val target = GroupPermission.find {
                (GroupsPermissions.groupId eq gid) and (GroupsPermissions.permissionId eq pid)
            }.firstOrNull() ?: throw notFound("($gid, $pid)")

            target.validUntil = validUntil
            target.negative = negative

            userGroupsRepo.memberAccountsHaveUpdated(gid)
            target.toModel()
        }
    }

    override fun getPermission(gid: UUID, pid: UUID): AssignedPermissionModel {
        return transaction {
            permissionRepo.ensureExistence(pid)

            GroupPermission.find {
                (GroupsPermissions.groupId eq gid) and
                (GroupsPermissions.permissionId eq pid)
            }.firstOrNull()?.toModel() ?: throw notFound("($gid, $pid)")
        }
    }

    override fun listPermissions(
        gid: UUID,
        cursor: PageCursorModel
    ): Pair<List<AssignedPermissionModel>, PageCursorModel> {
        return transaction {
            applyCursor(
                GroupsPermissions.join(Permissions, JoinType.INNER).selectAll(),
                cursor,
                Op.build { GroupsPermissions.groupId eq gid },
                GroupPermission
            ) { it.toModel() }
        }
    }

    override fun listActivePermissionNodes(gid: UUID): List<String> {
        return transaction {
            val nodes = mutableListOf<String>()

            // Iterate from highest to lowest group, then target
            // Adding permissions when they appear actively, removing them when they're blocked,
            // this also accounts for re-adding them after they have been blocked by
            // any of the parents above

            // [PAR3] -> [PAR2] -> [PAR1] -> [TARGET]
            groupInheritanceRepo.findParentGroupIds(gid).reversed().plus(gid).forEach { gID ->

                // Manually join and map to really get every last bit of efficiency
                // out of this, since it could result in a huge list potentially
                GroupsPermissions.innerJoin(Permissions)
                    // Only need the node itself, negative flag and valid-until stamp
                    .slice(GroupsPermissions.negative, GroupsPermissions.validUntil, Permissions.node)
                    .select { (GroupsPermissions.groupId eq gID) }
                    .forEach iter@{ row ->
                        val node = row[Permissions.node]
                        val validUntil = row[GroupsPermissions.validUntil]

                        // This permission is expired, skip it
                        if (validUntil != null && validUntil.isBeforeNow)
                            return@iter

                        // Add if positive, remove if negative
                        if (row[GroupsPermissions.negative]) nodes.remove(node) else nodes.add(node)
                    }
            }

            nodes
        }
    }

    override fun hasPermission(gid: UUID, pid: UUID): Boolean {
        return transaction {
            permissionRepo.ensureExistence(pid)
            var result = hasPermissionDirectly(gid, pid, true)

            // Group doesn't have this permission directly, search in inheritance tree
            if (!result) {
                // Get all parents, has order of distance ascending
                for (parentId in groupInheritanceRepo.findParentGroupIds(gid)) {
                    // Skip group if it doesn't have this permission
                    val curr = GroupPermission.find {
                        (GroupsPermissions.groupId eq parentId) and
                        (GroupsPermissions.permissionId eq pid)
                    }.firstOrNull() ?: continue

                    // Expired permission
                    if (curr.validUntil != null && curr.validUntil!!.isBeforeNow)
                        continue

                    // Block encountered, stop walking up the list
                    if (curr.negative)
                        break

                    // Active target permission is inherited
                    result = true
                    break
                }
            }

            result
        }
    }

    override fun hasPermissionDirectly(gid: UUID, pid: UUID, checkStamp: Boolean): Boolean {
        return transaction {
            permissionRepo.ensureExistence(pid)

            // Select permission entry, skip negative permissions
            val res = GroupsPermissions.select {
                (GroupsPermissions.groupId eq gid) and
                (GroupsPermissions.permissionId eq pid) and
                (GroupsPermissions.negative eq false)
            }

            // Check if still valid, based on arg-flag
            // Valid when validUntil > now() or validUntil is null
            if (checkStamp)
                res.andWhere { (GroupsPermissions.validUntil.greater(DateTime.now())) or (GroupsPermissions.validUntil.isNull()) }

            res.count() > 0
        }
    }

    override fun ensureExistence(gid: UUID, pid: UUID) {
        transaction {
            permissionRepo.ensureExistence(pid)

            val qry = GroupsPermissions.select {
                (GroupsPermissions.groupId eq gid) and
                (GroupsPermissions.permissionId eq pid)
            }

            if (qry.count() == 0L)
                throw notFound("($gid, $pid)")
        }
    }
}
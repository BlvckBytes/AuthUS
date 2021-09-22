package me.blvckbytes.authus.infrastructure.repo

import me.blvckbytes.authus.domain.exception.ModelCollisionException
import me.blvckbytes.authus.domain.model.AssignedPermissionModel
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import me.blvckbytes.authus.domain.repo.port.IGroupPermissionsRepo
import me.blvckbytes.authus.domain.repo.port.IPermissionRepo
import me.blvckbytes.authus.domain.repo.port.IUserAccountRepo
import me.blvckbytes.authus.domain.repo.port.IUserPermissionsRepo
import me.blvckbytes.authus.infrastructure.entity.auxiliary.UserGroup
import me.blvckbytes.authus.infrastructure.entity.auxiliary.UserPermission
import me.blvckbytes.authus.infrastructure.table.Permissions
import me.blvckbytes.authus.infrastructure.table.UserAccounts
import me.blvckbytes.authus.infrastructure.table.UsersGroups
import me.blvckbytes.authus.infrastructure.table.UsersPermissions
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import java.util.*

@Repository
open class UserPermissionsRepo(
    @Autowired @Lazy private val groupPermissionsRepo: IGroupPermissionsRepo,
    @Autowired @Lazy private val permissionRepo: IPermissionRepo,
    @Autowired @Lazy private val accountRepo: IUserAccountRepo,
) : ARepoBase<AssignedPermissionModel>("assigned_permission", UsersPermissions), IUserPermissionsRepo {

    override fun hasEffectivePermission(aid: UUID, pid: UUID): Boolean {
        return transaction {
            permissionRepo.ensureExistence(pid)

            // Find explicit permission assignment
            val explicit = UserPermission.find {
                (UsersPermissions.userId eq aid) and
                (UsersPermissions.permissionId eq pid)
            }.firstOrNull()?.toModel()

            // If there is an explicit permission, stop search and return with
            // true if not negative, false if negative (blocks group permission)
            if (explicit != null)
                return@transaction !explicit.negative

            // Find all memberships
            val memberships = UserGroup.find {
                (UsersGroups.userId eq aid) and
                (UsersGroups.validUntil.greater(DateTime.now()) or UsersGroups.validUntil.isNull())
            }

            // Initial result is false, then search
            var result = false
            for (membership in memberships) {
                val currGroup = membership.groupId.value

                // Active permission encountered, stop search
                if (groupPermissionsRepo.hasPermission(currGroup, pid)) {
                    result = true
                    break
                }
            }

            result
        }
    }

    override fun appendExplicitPermission(aid: UUID, pid: UUID, validUntil: DateTime?, negative: Boolean ): AssignedPermissionModel {
        return transaction {
            permissionRepo.ensureExistence(pid)

            // Explicit permission has already been assigned
            if (
                UsersPermissions.select {
                    (UsersPermissions.userId eq aid) and
                    (UsersPermissions.permissionId eq pid)
                }.count() > 0
            )
                throw ModelCollisionException("explicit_permission", "(id, pid)", "($aid, $pid)")

            val created = UserPermission.new {
                userId = EntityID(aid, UserAccounts)
                this.permissionId = EntityID(pid, Permissions)
                this.validUntil = validUntil
                this.negative = negative
            }.toModel()

            accountRepo.accountHasUpdated(aid)
            created
        }
    }

    override fun updateExplicitPermission(aid: UUID, pid: UUID, validUntil: DateTime?, negative: Boolean): AssignedPermissionModel {
        return transaction {
            permissionRepo.ensureExistence(pid)

            val target = UserPermission.find {
                (UsersPermissions.userId eq aid) and
                (UsersPermissions.permissionId eq pid)
            }.firstOrNull() ?: throw notFound("($aid, $pid)")

            target.validUntil = validUntil
            target.negative = negative

            accountRepo.accountHasUpdated(aid)
            target.toModel()
        }
    }

    override fun getExplicitPermission(aid: UUID, pid: UUID): AssignedPermissionModel {
        return transaction {
            permissionRepo.ensureExistence(pid)

            UserPermission.find {
                (UsersPermissions.userId eq aid) and
                (UsersPermissions.permissionId eq pid)
            }.firstOrNull()?.toModel() ?: throw notFound("($aid, $pid)")
        }
    }

    override fun removeExplicitPermission(aid: UUID, pid: UUID) {
        return transaction {
            permissionRepo.ensureExistence(pid)

            if (
                UsersPermissions.deleteWhere {
                    (UsersPermissions.userId eq aid) and
                    (UsersPermissions.permissionId eq pid)
                } == 0
            )
                throw notFound("($aid, $pid)")

            accountRepo.accountHasUpdated(aid)
        }
    }

    override fun listExplicitPermissions(aid: UUID, cursor: PageCursorModel): Pair<List<AssignedPermissionModel>, PageCursorModel> {
        return transaction {
            applyCursor(
                UsersPermissions.leftJoin(Permissions).selectAll(),
                cursor,
                Op.build { UsersPermissions.userId eq aid },
                UserPermission
            ) { it.toModel() }
        }
    }

    override fun listActiveExplicitPermissionData(aid: UUID): List<Pair<String, Boolean>> {
        return transaction {
            UserPermission.find {
                (UsersPermissions.userId eq aid) and
                (UsersPermissions.validUntil.greater(DateTime.now()))
            }.map { Pair(it.permission.node, it.negative) }
        }
    }
}
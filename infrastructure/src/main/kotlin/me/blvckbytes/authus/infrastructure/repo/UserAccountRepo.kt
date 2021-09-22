package me.blvckbytes.authus.infrastructure.repo

import me.blvckbytes.authus.domain.model.UserAccountInputModel
import me.blvckbytes.authus.domain.model.UserAccountModel
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import me.blvckbytes.authus.domain.repo.port.IGroupPermissionsRepo
import me.blvckbytes.authus.domain.repo.port.IUserAccountRepo
import me.blvckbytes.authus.domain.repo.port.IUserGroupsRepo
import me.blvckbytes.authus.domain.repo.port.IUserPermissionsRepo
import me.blvckbytes.authus.infrastructure.entity.UserAccount
import me.blvckbytes.authus.infrastructure.table.UserAccounts
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import java.util.*

@Repository
open class UserAccountRepo(
    @Autowired @Lazy private val userGroupsRepo: IUserGroupsRepo,
    @Autowired @Lazy private val groupPermissionsRepo: IGroupPermissionsRepo,
    @Autowired @Lazy private val userPermissionsRepo: IUserPermissionsRepo
) : IUserAccountRepo, ARepoBase<UserAccountModel>("user_account", UserAccounts) {

    override fun createAccount(account: UserAccountInputModel): UserAccountModel {
        return transaction {

            // Account with this username already exists
            if (UserAccounts.select { strColEq(UserAccounts.username, account.username) }.count() > 0)
                throw collision("username", account.username)

            // Account with this email already exists
            if (UserAccounts.select { strColEq(UserAccounts.email, account.email) }.count() > 0)
                throw collision("email", account.email)

            UserAccount.new {
                username = account.username
                this.passHash = account.hashPassword(account.password)
                email = account.email
                updatedAt = DateTime.now()
            }.toModel()
        }
    }

    override fun deleteAccount(id: UUID) {
        transaction {
            if (UserAccounts.deleteWhere { UserAccounts.id eq id } == 0)
                throw notFound(id)
        }
    }

    override fun getAccount(id: UUID): UserAccountModel {
        return transaction {
            UserAccount.findById(id)?.toModel() ?: throw notFound(id)
        }
    }

    override fun getAccount(username: String): UserAccountModel {
        return transaction {
            UserAccount
                .find { strColEq(UserAccounts.username, username) }
                .firstOrNull()
                ?.toModel() ?: throw notFound(username)
        }
    }

    override fun updateAccount(id: UUID, account: UserAccountInputModel): UserAccountModel {
        return transaction {

            // The new username would collide with an existing username
            if (
                UserAccounts.select {
                    strColEq(UserAccounts.username, account.username) and
                    (UserAccounts.id neq id)
                }.count() > 0
            )
                throw collision("username", account.username)

            // The new email would collide with an existing email
            if (
                UserAccounts.select {
                    strColEq(UserAccounts.email, account.email) and
                    (UserAccounts.id neq id)
                }.count() > 0
            )
                throw collision("email", account.email)

            val target = UserAccount.findById(id) ?: throw notFound(id)
            target.username = account.username
            target.passHash = account.hashPassword(account.password)
            target.email = account.email

            accountHasUpdated(id)
            target.toModel()
        }
    }

    override fun accountHasUpdated(id: UUID) {
        transaction {
            val target = UserAccount.findById(id) ?: throw notFound(id)
            target.updatedAt = DateTime.now()
        }
    }

    override fun listEffectivePermissionNodes(aid: UUID): List<String> {
        // List all group IDs this account is a member of
        val activeNodes = userGroupsRepo.listMembershipGroupIds(aid)

            // Map every ID to a list of active nodes
            .map { groupPermissionsRepo.listActivePermissionNodes(it) }

            // Flatten list
            .fold(listOf<String>()) { acc, curr -> acc.plus(curr) }.toMutableList()

        // List all active account permissions
        userPermissionsRepo.listActiveExplicitPermissionData(aid).forEach {
            if (it.second) activeNodes.remove(it.first) else activeNodes.add(it.first)
        }

        // Remove duplicate nodes ignore-case
        return activeNodes.distinctBy { it.uppercase() }
    }

    override fun listAccounts(cursor: PageCursorModel): Pair<List<UserAccountModel>, PageCursorModel> {
        return transaction {
            applyCursor(UserAccounts.selectAll(), cursor, null, UserAccount) { it.toModel() }
        }
    }

    override fun ensureExistence(id: UUID) {
        transaction {
            if (UserAccounts.select { UserAccounts.id eq id }.count() == 0L)
                throw notFound(id)
        }
    }

    override fun isStampLatest(id: UUID, stamp: DateTime): Boolean {
        return transaction {
            val targetRow = UserAccounts
                .slice(UserAccounts.updatedAt)
                .select { UserAccounts.id eq id }
                .firstOrNull() ?: throw notFound(id)

            targetRow[UserAccounts.updatedAt] == stamp
        }
    }
}
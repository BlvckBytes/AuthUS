package me.blvckbytes.authus.domain.repo.port

import me.blvckbytes.authus.domain.model.UserAccountInputModel
import me.blvckbytes.authus.domain.model.UserAccountModel
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import org.joda.time.DateTime
import java.util.*

/**
 * Responsible for creating, deleting, updating and fetching accounts
 * Also calculates effective permissions using user-permission and
 * user-group information, can update the account hash and check
 * if one is valid
 */
interface IUserAccountRepo {

    /**
     * Create a new account
     * @param account Values to save
     */
    fun createAccount(account: UserAccountInputModel): UserAccountModel

    /**
     * Delete an account by it's ID
     * @param id ID of the target account
     */
    fun deleteAccount(id: UUID)

    /**
     * Get an account by it's username
     * @param username Username of the target account
     */
    fun getAccount(username: String): UserAccountModel

    /**
     * Get an account by it's ID
     * @param id ID of the target account
     */
    fun getAccount(id: UUID): UserAccountModel

    /**
     * Update an account by it's ID
     * @param id ID of the target account
     * @param account New state to update to
     */
    fun updateAccount(id: UUID, account: UserAccountInputModel): UserAccountModel

    /**
     * List all available accounts
     * @param cursor Cursor containing search-information
     */
    fun listAccounts(cursor: PageCursorModel): Pair<List<UserAccountModel>, PageCursorModel>

    /**
     * Update the account update stamp to current time
     */
    fun accountHasUpdated(id: UUID)

    /**
     * List all effective permission nodes an account has
     * through direct attachments or group inheritance
     * @param aid ID of target account
     */
    fun listEffectivePermissionNodes(aid: UUID): List<String>

    /**
     * Checks whether or not this stamp is the latest update of account
     * @param id ID of the target user
     * @param stamp DateTime value to compare
     */
    fun isStampLatest(id: UUID, stamp: DateTime): Boolean

    /**
     * Ensure that the account exists, raises an exception otherwise
     * @param id ID of the account in question
     */
    fun ensureExistence(id: UUID)
}
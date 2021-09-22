package me.blvckbytes.authus.infrastructure.table

/**
 * Represents the account of a user
 */
object UserAccounts : BaseUUIDTable("user_accounts") {
    var username = varchar("username", 255, "BINARY")
    var email = varchar("email", 255)
    var passHash = varchar("pass_hash", 255)

    init {
        // An account with a certain username and email combination can only exist once
        index(true, username, email)
    }
}
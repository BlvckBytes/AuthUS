package me.blvckbytes.authus.domain.model

import at.favre.lib.crypto.bcrypt.BCrypt
import org.joda.time.DateTime
import java.util.*

class UserAccountModel(
    var id: UUID,
    var username: String,
    var passHash: String?,
    var email: String,
    var joined: DateTime,
    var updatedAt: DateTime
) {
    fun checkPassword(password: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), passHash?.toCharArray()).verified
    }
}
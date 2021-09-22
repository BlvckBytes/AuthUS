package me.blvckbytes.authus.domain.model

import at.favre.lib.crypto.bcrypt.BCrypt
import java.nio.charset.StandardCharsets

class UserAccountInputModel(
    var username: String,
    var password: String,
    var email: String
) {
    fun hashPassword(password: String): String {
        return String(BCrypt.withDefaults().hashToChar(12, password.toCharArray()))
    }
}
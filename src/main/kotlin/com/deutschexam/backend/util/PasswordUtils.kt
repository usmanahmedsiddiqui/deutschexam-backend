package com.deutschexam.backend.util

import org.mindrot.jbcrypt.BCrypt

object PasswordUtils {
    fun hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(12))
    fun verify(password: String, hash: String): Boolean = BCrypt.checkpw(password, hash)
}

package com.tuempresa.osornomarket.backend.security

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordHasher {
    fun hash(password: String): String = 
        BCrypt.withDefaults().hashToString(12, password.toCharArray())

    fun check(password: String, hash: String): Boolean = 
        BCrypt.verifyer().verify(password.toCharArray(), hash).verified
}

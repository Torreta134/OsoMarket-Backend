package com.tuempresa.osornomarket.backend.domain.repository

import com.tuempresa.osornomarket.backend.domain.model.User

interface UserRepositoryContract {
    suspend fun findByEmail(email: String): User?
    suspend fun findById(id: Int): User?
    suspend fun create(user: User): Int
}

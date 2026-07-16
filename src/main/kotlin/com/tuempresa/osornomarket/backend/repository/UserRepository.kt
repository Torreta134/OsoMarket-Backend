package com.tuempresa.osornomarket.backend.repository

import com.tuempresa.osornomarket.backend.data.database.UsersTable
import com.tuempresa.osornomarket.backend.data.mapper.toDomainUser
import com.tuempresa.osornomarket.backend.domain.model.User
import com.tuempresa.osornomarket.backend.domain.repository.UserRepositoryContract
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserRepository : UserRepositoryContract {
    override suspend fun findByEmail(email: String): User? = newSuspendedTransaction(Dispatchers.IO) {
        UsersTable.select { UsersTable.email eq email }
            .map { it.toDomainUser() }
            .singleOrNull()
    }

    override suspend fun findById(id: Int): User? = newSuspendedTransaction(Dispatchers.IO) {
        UsersTable.select { UsersTable.id eq id }
            .map { it.toDomainUser() }
            .singleOrNull()
    }

    override suspend fun create(user: User): Int = newSuspendedTransaction(Dispatchers.IO) {
        UsersTable.insert {
            it[name] = user.name
            it[email] = user.email
            it[rut] = user.rut
            it[password] = user.passwordHash
        } get UsersTable.id
    }
}

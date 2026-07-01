package com.tuempresa.osornomarket.backend.data.mapper

import com.tuempresa.osornomarket.backend.data.database.UsersTable
import com.tuempresa.osornomarket.backend.data.dto.UserDto
import com.tuempresa.osornomarket.backend.domain.model.User
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toDomainUser() = User(
    id = this[UsersTable.id],
    name = this[UsersTable.name],
    email = this[UsersTable.email],
    passwordHash = this[UsersTable.password]
)

fun User.toDto() = UserDto(
    id = id,
    name = name,
    email = email
)

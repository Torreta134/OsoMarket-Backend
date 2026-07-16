package com.tuempresa.osornomarket.backend.data.database

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val email = varchar("email", 100).uniqueIndex()
    val rut = varchar("rut", 20).uniqueIndex()
    val password = varchar("password", 200)

    override val primaryKey = PrimaryKey(id)
}

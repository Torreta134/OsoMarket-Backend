package com.tuempresa.osornomarket.backend.data.database

import org.jetbrains.exposed.sql.Table

object ProductsTable : Table("products") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val brand = varchar("brand", 100)
    val description = text("description")
    val price = long("price")
    val sellerId = varchar("seller_id", 100)
    val imageUrl = text("image_url")
    val condition = varchar("condition", 50)

    override val primaryKey = PrimaryKey(id)
}

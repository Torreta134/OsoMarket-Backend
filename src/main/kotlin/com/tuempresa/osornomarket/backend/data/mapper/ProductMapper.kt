package com.tuempresa.osornomarket.backend.data.mapper

import com.tuempresa.osornomarket.backend.data.database.ProductsTable
import com.tuempresa.osornomarket.backend.data.dto.ProductDto
import com.tuempresa.osornomarket.backend.domain.model.Product
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toDomainProduct() = Product(
    id = this[ProductsTable.id],
    name = this[ProductsTable.name],
    brand = this[ProductsTable.brand],
    description = this[ProductsTable.description],
    price = this[ProductsTable.price],
    type = this[ProductsTable.type],
    sellerId = this[ProductsTable.sellerId],
    imageUrl = this[ProductsTable.imageUrl],
    condition = this[ProductsTable.condition]
)

fun Product.toDto() = ProductDto(
    id = id,
    name = name,
    brand = brand,
    description = description,
    price = price,
    type = type,
    sellerId = sellerId,
    imageUrl = imageUrl,
    condition = condition
)

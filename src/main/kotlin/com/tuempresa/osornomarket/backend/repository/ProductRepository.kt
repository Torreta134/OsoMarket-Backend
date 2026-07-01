package com.tuempresa.osornomarket.backend.repository

import com.tuempresa.osornomarket.backend.data.database.ProductsTable
import com.tuempresa.osornomarket.backend.data.mapper.toDomainProduct
import com.tuempresa.osornomarket.backend.domain.model.Product
import com.tuempresa.osornomarket.backend.domain.repository.ProductRepositoryContract
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ProductRepository : ProductRepositoryContract {
    override suspend fun getAll(): List<Product> = newSuspendedTransaction(Dispatchers.IO) {
        ProductsTable.selectAll().map { it.toDomainProduct() }
    }

    override suspend fun getById(id: Int): Product? = newSuspendedTransaction(Dispatchers.IO) {
        ProductsTable.select { ProductsTable.id eq id }
            .map { it.toDomainProduct() }
            .singleOrNull()
    }

    override suspend fun create(product: Product): Int = newSuspendedTransaction(Dispatchers.IO) {
        ProductsTable.insert {
            it[name] = product.name
            it[brand] = product.brand
            it[description] = product.description
            it[price] = product.price
            it[sellerId] = product.sellerId
            it[imageUrl] = product.imageUrl
            it[condition] = product.condition
        } get ProductsTable.id
    }

    override suspend fun update(id: Int, product: Product): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        ProductsTable.update({ ProductsTable.id eq id }) {
            it[name] = product.name
            it[brand] = product.brand
            it[description] = product.description
            it[price] = product.price
            it[sellerId] = product.sellerId
            it[imageUrl] = product.imageUrl
            it[condition] = product.condition
        } > 0
    }

    override suspend fun delete(id: Int): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        ProductsTable.deleteWhere { ProductsTable.id eq id } > 0
    }
}


package com.tuempresa.osornomarket.backend.domain.repository

import com.tuempresa.osornomarket.backend.domain.model.Product

interface ProductRepositoryContract {
    suspend fun getAll(): List<Product>
    suspend fun getById(id: Int): Product?
    suspend fun create(product: Product): Int
    suspend fun update(id: Int, product: Product): Boolean
    suspend fun delete(id: Int): Boolean
}


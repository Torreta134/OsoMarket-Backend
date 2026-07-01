package com.tuempresa.osornomarket.backend.service

import com.tuempresa.osornomarket.backend.data.dto.CreateProductRequest
import com.tuempresa.osornomarket.backend.data.dto.ProductDto
import com.tuempresa.osornomarket.backend.data.mapper.toDto
import com.tuempresa.osornomarket.backend.domain.model.Product
import com.tuempresa.osornomarket.backend.domain.repository.ProductRepositoryContract

class ProductService(private val repository: ProductRepositoryContract) {
    
    suspend fun getAllProducts(): List<ProductDto> {
        return repository.getAll().map { it.toDto() }
    }

    suspend fun getProductById(id: Int): ProductDto {
        val product = repository.getById(id) ?: throw NotFoundException("Producta no encontrado")
        return product.toDto()
    }

    suspend fun createProduct(req: CreateProductRequest): Int {
        val product = Product(
            id = 0,
            name = req.name,
            brand = req.brand,
            description = req.description,
            price = req.price,
            sellerId = req.sellerId,
            imageUrl = req.imageUrl,
            condition = req.condition
        )
        return repository.create(product)
    }

    suspend fun updateProduct(id: Int, req: CreateProductRequest) {
        val product = Product(
            id = id,
            name = req.name,
            brand = req.brand,
            description = req.description,
            price = req.price,
            sellerId = req.sellerId,
            imageUrl = req.imageUrl,
            condition = req.condition
        )
        if (!repository.update(id, product)) {
            throw NotFoundException("No se pudo actualizar: Producta no encontrado")
        }
    }

    suspend fun deleteProduct(id: Int) {
        if (!repository.delete(id)) {
            throw NotFoundException("No se pudo eliminar: Producta no encontrado")
        }
    }
}


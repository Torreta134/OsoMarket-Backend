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

    suspend fun createProduct(req: CreateProductRequest, sellerId: String): Int {
        val product = Product(
            id = 0,
            name = req.name,
            brand = req.brand,
            description = req.description,
            price = req.price,
            type = req.type,
            sellerId = sellerId,
            imageUrl = req.imageUrl,
            condition = req.condition
        )
        return repository.create(product)
    }

    suspend fun updateProduct(id: Int, req: CreateProductRequest, sellerId: String) {
        val existing = repository.getById(id) ?: throw NotFoundException("Producto no encontrado")
        if (existing.sellerId != sellerId) throw UnauthorizedException("No tienes permiso para editar este producto")
        
        val product = Product(
            id = id,
            name = req.name,
            brand = req.brand,
            description = req.description,
            price = req.price,
            type = req.type,
            sellerId = sellerId,
            imageUrl = req.imageUrl,
            condition = req.condition
        )
        repository.update(id, product)
    }

    suspend fun deleteProduct(id: Int, sellerId: String) {
        val existing = repository.getById(id) ?: throw NotFoundException("Producto no encontrado")
        if (existing.sellerId != sellerId) throw UnauthorizedException("No tienes permiso para eliminar este producto")
        repository.delete(id)
    }
}


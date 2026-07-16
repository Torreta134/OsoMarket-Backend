package com.tuempresa.osornomarket.backend.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Int,
    val name: String,
    val brand: String,
    val description: String,
    val price: Long,
    val type: String,
    val sellerId: String,
    val imageUrl: String,
    val condition: String
)

@Serializable
data class CreateProductRequest(
    val name: String,
    val brand: String,
    val description: String,
    val price: Long,
    val type: String,
    val imageUrl: String,
    val condition: String
)

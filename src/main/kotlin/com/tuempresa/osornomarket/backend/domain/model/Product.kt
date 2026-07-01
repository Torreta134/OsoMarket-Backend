package com.tuempresa.osornomarket.backend.domain.model

data class Product(
    val id: Int,
    val name: String,
    val brand: String,
    val description: String,
    val price: Long,
    val sellerId: String,
    val imageUrl: String,
    val condition: String
)

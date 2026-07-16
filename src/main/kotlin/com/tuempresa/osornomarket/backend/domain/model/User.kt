package com.tuempresa.osornomarket.backend.domain.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val rut: String,
    val passwordHash: String
)

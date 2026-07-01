package com.tuempresa.osornomarket.backend.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val email: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

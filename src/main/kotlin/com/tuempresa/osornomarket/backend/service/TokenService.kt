package com.tuempresa.osornomarket.backend.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.*

class TokenService(config: ApplicationConfig) {
    private val secret = config.property("jwt.secret").getString()
    private val issuer = config.property("jwt.issuer").getString()
    private val audience = config.property("jwt.audience").getString()
    private val algorithm = Algorithm.HMAC256(secret)

    fun generate(id: Int, email: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("id", id)
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000)) // 1 hora
            .sign(algorithm)
    }

    fun getVerifier() = JWT
        .require(algorithm)
        .withAudience(audience)
        .withIssuer(issuer)
        .build()
}

package com.tuempresa.osornomarket.backend.service

import com.tuempresa.osornomarket.backend.data.dto.LoginRequest
import com.tuempresa.osornomarket.backend.data.dto.RegisterRequest
import com.tuempresa.osornomarket.backend.domain.model.User
import com.tuempresa.osornomarket.backend.domain.repository.UserRepositoryContract
import com.tuempresa.osornomarket.backend.security.PasswordHasher

class AuthService(
    private val repository: UserRepositoryContract,
    private val tokenService: TokenService
) {
    suspend fun register(req: RegisterRequest): String {
        if (repository.findByEmail(req.email) != null) {
            throw ConflictException("El email ya está registrado")
        }
        
        val passwordHash = PasswordHasher.hash(req.password)
        val id = repository.create(
            User(
                id = 0,
                name = req.name,
                email = req.email,
                passwordHash = passwordHash
            )
        )
        
        return tokenService.generate(id, req.email)
    }

    suspend fun login(req: LoginRequest): String {
        val user = repository.findByEmail(req.email) 
            ?: throw UnauthorizedException("Credenciales inválidas")
            
        if (!PasswordHasher.check(req.password, user.passwordHash)) {
            throw UnauthorizedException("Credenciales inválidas")
        }
        
        return tokenService.generate(user.id, user.email)
    }
}

package com.tuempresa.osornomarket.backend.routes

import com.tuempresa.osornomarket.backend.data.dto.LoginRequest
import com.tuempresa.osornomarket.backend.data.dto.LoginResponse
import com.tuempresa.osornomarket.backend.data.dto.RegisterRequest
import com.tuempresa.osornomarket.backend.service.AuthService
import com.tuempresa.osornomarket.backend.service.ConflictException
import com.tuempresa.osornomarket.backend.service.UnauthorizedException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    
    post("/auth/register") {
        try {
            val req = call.receive<RegisterRequest>()
            val token = authService.register(req)
            call.respond(HttpStatusCode.Created, LoginResponse(token))
        } catch (e: ConflictException) {
            call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
        }
    }

    post("/auth/login") {
        try {
            val req = call.receive<LoginRequest>()
            val token = authService.login(req)
            call.respond(HttpStatusCode.OK, LoginResponse(token))
        } catch (e: UnauthorizedException) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.message))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
        }
    }
}

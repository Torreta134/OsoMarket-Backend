package com.tuempresa.osornomarket.backend.routes

import com.tuempresa.osornomarket.backend.data.dto.CreateProductRequest
import com.tuempresa.osornomarket.backend.service.NotFoundException
import com.tuempresa.osornomarket.backend.service.ProductService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.jwt.JWTPrincipal
import com.tuempresa.osornomarket.backend.service.UnauthorizedException

fun Route.productRoutes(productService: ProductService) {

    route("/products") {
        
        get {
            val products = productService.getAllProducts()
            call.respond(products)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "ID inválido")
                return@get
            }
            try {
                val product = productService.getProductById(id)
                call.respond(product)
            } catch (e: NotFoundException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
            }
        }

        // Rutas que requieren autenticación
        authenticate("auth-jwt") {
            
            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("id")?.asInt()?.toString()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, "No autorizado")
                    return@post
                }
                
                val req = call.receive<CreateProductRequest>()
                val id = productService.createProduct(req, userId)
                call.respond(HttpStatusCode.Created, mapOf("id" to id))
            }

            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("id")?.asInt()?.toString()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, "No autorizado")
                    return@put
                }
                
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "ID inválido")
                    return@put
                }
                try {
                    val req = call.receive<CreateProductRequest>()
                    productService.updateProduct(id, req, userId)
                    call.respond(HttpStatusCode.OK, "Producto actualizado")
                } catch (e: NotFoundException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: UnauthorizedException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                }
            }

            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("id")?.asInt()?.toString()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, "No autorizado")
                    return@delete
                }
                
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "ID inválido")
                    return@delete
                }
                try {
                    productService.deleteProduct(id, userId)
                    call.respond(HttpStatusCode.OK, "Producto eliminado")
                } catch (e: NotFoundException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: UnauthorizedException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                }
            }
        }
    }
}


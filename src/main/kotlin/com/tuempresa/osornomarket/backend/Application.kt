    package com.tuempresa.osornomarket.backend

import com.tuempresa.osornomarket.backend.data.database.ProductsTable
import com.tuempresa.osornomarket.backend.data.database.UsersTable
import com.tuempresa.osornomarket.backend.repository.ProductRepository
import com.tuempresa.osornomarket.backend.repository.UserRepository
import com.tuempresa.osornomarket.backend.routes.authRoutes
import com.tuempresa.osornomarket.backend.routes.productRoutes
import com.tuempresa.osornomarket.backend.service.AuthService
import com.tuempresa.osornomarket.backend.service.ProductService
import com.tuempresa.osornomarket.backend.service.TokenService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    
    // Configuración de Content Negotiation (JSON)
    install(ContentNegotiation) {
        json()
    }

    // Inicializar DB
    val dataSource = initDatabase()
    
    // Cerrar pool al detener la aplicación
    monitor.subscribe(ApplicationStopped) {
        dataSource.close()
    }

    // Servicios
    val tokenService = TokenService(this.environment.config)
    val userRepository = UserRepository()
    val productRepository = ProductRepository()
    
    val authService = AuthService(userRepository, tokenService)
    val productService = ProductService(productRepository)

    // Configuración de Autenticación JWT
    install(Authentication) {
        jwt("auth-jwt") {
            realm = this@module.environment.config.property("jwt.realm").getString()
            verifier(tokenService.getVerifier())
            validate { credential ->
                if (credential.payload.getClaim("id").asInt() != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }

    // Rutas
    routing {
        authRoutes(authService)
        productRoutes(productService)
    }
}

fun Application.initDatabase(): HikariDataSource {
    val driverClassName = this.environment.config.property("storage.driverClassName").getString()
    val jdbcURL = this.environment.config.property("storage.jdbcURL").getString()
    val user = this.environment.config.property("storage.user").getString()
    val password = this.environment.config.property("storage.password").getString()

    val hikariConfig = HikariConfig().apply {
        this.driverClassName = driverClassName
        this.jdbcUrl = jdbcURL
        this.username = user
        this.password = password
        this.maximumPoolSize = 10
        this.isReadOnly = false
        this.transactionIsolation = "TRANSACTION_READ_COMMITTED"
        this.validate()
    }

    val dataSource = HikariDataSource(hikariConfig)
    Database.connect(dataSource)

    transaction {
        SchemaUtils.createMissingTablesAndColumns(UsersTable, ProductsTable)
    }
    
    return dataSource
}


# 🐻 OsoMarket Backend

Backend del proyecto **OsoMarket** desarrollado con **Ktor** (Kotlin) siguiendo los principios de **Arquitectura Limpia (Clean Architecture)** con separación estricta de capas.

---

## 📐 Arquitectura del Proyecto

El proyecto implementa una **Arquitectura Limpia** con flujo de datos **unidireccional**. Cada capa tiene una responsabilidad única y se comunica exclusivamente con la capa adyacente.

### Flujo de datos

```
Routes  →  Services  →  Repositories (implementan Contracts)  →  Database (Exposed Tables)
                                         ↕
                                      Mappers
                                         ↕
                                   Domain Models
```

> **Regla fundamental:** Las `Routes` **nunca** acceden directamente a los `Repositories`. Siempre pasan por la capa de `Services`, que contiene la lógica de negocio.

---

## 📁 Estructura de Carpetas

```
backend/src/main/kotlin/com/tuempresa/osornomarket/backend/
│
├── Application.kt                      # Punto de entrada, configuración de Ktor
│
├── data/                               # Capa de Datos
│   ├── database/                       # Tablas de Exposed (ORM)
│   │   ├── UsersTable.kt
│   │   └── ProgramsTable.kt
│   │
│   ├── dto/                            # Data Transfer Objects (Request/Response)
│   │   ├── UserDtos.kt                 # LoginRequest, LoginResponse, RegisterRequest, etc.
│   │   └── ProgramDtos.kt             # ProgramDto, CreateProgramRequest
│   │
│   └── mapper/                         # Conversores entre capas
│       ├── UserMapper.kt              # ResultRow → User, User → UserDto
│       └── ProgramMapper.kt          # ResultRow → Program, Program → ProgramDto
│
├── domain/                             # Capa de Dominio (modelos puros, sin dependencias externas)
│   ├── model/
│   │   ├── User.kt                    # data class User
│   │   └── Program.kt                # data class Program
│   │
│   └── repository/                    # Contratos (Interfaces)
│       ├── UserRepositoryContract.kt
│       └── ProgramRepositoryContract.kt
│
├── repository/                         # Implementación de los Contratos
│   ├── UserRepository.kt             # : UserRepositoryContract
│   └── ProgramRepository.kt          # : ProgramRepositoryContract
│
├── service/                            # Lógica de Negocio
│   ├── AuthService.kt                 # Registro, Login (usa PasswordHasher + TokenService)
│   ├── ProgramService.kt             # CRUD de Programas
│   ├── TokenService.kt               # Generación y verificación de JWT
│   └── Exceptions.kt                 # ConflictException, UnauthorizedException, NotFoundException
│
├── security/                           # Seguridad
│   └── PasswordHasher.kt             # Hashing con BCrypt (hash + check)
│
└── routes/                             # Endpoints HTTP
    ├── AuthRoutes.kt                  # POST /auth/register, POST /auth/login
    └── ProgramRoutes.kt              # GET, POST, PUT, DELETE /programs
```

---

## 🔄 Flujo Detallado por Capa

### Ejemplo: Crear un Programa (POST /programs)

```
1. ProgramRoutes.kt          → Recibe el request HTTP, deserializa CreateProgramRequest
2. ProgramService.kt         → Valida datos, construye el modelo Program de dominio
3. ProgramRepository.kt      → Ejecuta ProgramsTable.insert{} con Exposed dentro de transaction
4. ProgramMapper.kt          → Convierte ResultRow → Program (modelo de dominio)
5. ProgramMapper.kt          → Convierte Program → ProgramDto (respuesta al cliente)
6. ProgramRoutes.kt          → Responde con HttpStatusCode.Created
```

### Ejemplo: Login de Usuario (POST /auth/login)

```
1. AuthRoutes.kt             → Recibe LoginRequest (email + password)
2. AuthService.kt            → Busca usuario por email vía UserRepositoryContract
3. UserRepository.kt         → Ejecuta UsersTable.select{} con Exposed
4. UserMapper.kt             → Convierte ResultRow → User (modelo de dominio)
5. AuthService.kt            → Verifica contraseña con PasswordHasher.check()
6. TokenService.kt           → Genera JWT con claims (id, email) y expiración
7. AuthRoutes.kt             → Responde con LoginResponse(token)
```

---

## 🗄️ Base de Datos — Neon (PostgreSQL)

- **Proveedor:** [Neon](https://neon.tech/) (PostgreSQL serverless)
- **ORM:** Exposed (JetBrains)
- **Connection Pool:** HikariCP
- **Creación automática de tablas:** `SchemaUtils.create(UsersTable, ProgramsTable)` al iniciar la aplicación

### Tablas

| Tabla | Columnas | Descripción |
|-------|----------|-------------|
| `users` | `id` (PK, auto), `name`, `email` (unique), `password` | Usuarios registrados |
| `programs` | `id` (PK, auto), `name`, `description`, `price` | Programas del marketplace |

La configuración de conexión se encuentra en `application.conf` bajo la sección `storage`.

---

## 🔐 Capa de Seguridad

### Password Hashing (BCrypt)

| Acción | Método | Ubicación |
|--------|--------|-----------|
| Hashear contraseña al registrar | `PasswordHasher.hash(password)` | `AuthService.register()` |
| Verificar contraseña al iniciar sesión | `PasswordHasher.check(password, hash)` | `AuthService.login()` |

- Librería: `at.favre.lib:bcrypt`
- Cost factor: 12 rounds
- **La contraseña se hashea ANTES de guardarse en la base de datos**

### JWT (JSON Web Tokens)

| Acción | Método | Ubicación |
|--------|--------|-----------|
| Generar token | `TokenService.generate(id, email)` | `AuthService.register()` / `login()` |
| Verificar token | `TokenService.getVerifier()` | `Application.kt` — plugin `Authentication` |

- Librería: `com.auth0:java-jwt`
- Claims: `id` (Int), `email` (String)
- Expiración: 1 hora
- Algoritmo: HMAC256
- Configuración (`secret`, `issuer`, `audience`, `realm`) en `application.conf` sección `jwt`

### Rutas Protegidas

Las operaciones de escritura sobre `programs` requieren autenticación JWT:

```
🔓 GET  /programs          → Pública
🔓 GET  /programs/{id}     → Pública
🔒 POST /programs          → Requiere JWT
🔒 PUT  /programs/{id}     → Requiere JWT
🔒 DELETE /programs/{id}   → Requiere JWT
```

---

## 🌐 Endpoints (API REST)

### Autenticación (`/auth`)

| Método | Ruta | Body (Request) | Respuesta | Código |
|--------|------|-----------------|-----------|--------|
| POST | `/auth/register` | `RegisterRequest { name, email, password }` | `LoginResponse { token }` | `201 Created` |
| POST | `/auth/login` | `LoginRequest { email, password }` | `LoginResponse { token }` | `200 OK` |

### Programas (`/programs`)

| Método | Ruta | Auth | Body (Request) | Respuesta | Código |
|--------|------|------|-----------------|-----------|--------|
| GET | `/programs` | No | — | `List<ProgramDto>` | `200 OK` |
| GET | `/programs/{id}` | No | — | `ProgramDto` | `200 OK` |
| POST | `/programs` | 🔒 JWT | `CreateProgramRequest { name, description, price }` | `{ id: Int }` | `201 Created` |
| PUT | `/programs/{id}` | 🔒 JWT | `CreateProgramRequest { name, description, price }` | `"Programa actualizado"` | `200 OK` |
| DELETE | `/programs/{id}` | 🔒 JWT | — | `"Programa eliminado"` | `200 OK` |

---

## 📦 DTOs (Data Transfer Objects)

### DTOs de Seguridad

```kotlin
// Request
data class RegisterRequest(val name: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)

// Response
data class LoginResponse(val token: String)
data class UserDto(val id: Int, val name: String, val email: String)
```

### DTOs de Programas

```kotlin
// Request
data class CreateProgramRequest(val name: String, val description: String, val price: Long)

// Response
data class ProgramDto(val id: Int, val name: String, val description: String, val price: Long)
```

> Todos los DTOs están anotados con `@Serializable` de Kotlinx Serialization.

---

## 🧩 Abstracción — Contratos e Implementaciones

### Interfaces (Contratos)

```kotlin
// domain/repository/UserRepositoryContract.kt
interface UserRepositoryContract {
    suspend fun findByEmail(email: String): User?
    suspend fun findById(id: Int): User?
    suspend fun create(user: User): Int
}

// domain/repository/ProgramRepositoryContract.kt
interface ProgramRepositoryContract {
    suspend fun getAll(): List<Program>
    suspend fun getById(id: Int): Program?
    suspend fun create(program: Program): Int
    suspend fun update(id: Int, program: Program): Boolean
    suspend fun delete(id: Int): Boolean
}
```

### Implementaciones

```kotlin
class UserRepository : UserRepositoryContract { /* ... */ }
class ProgramRepository : ProgramRepositoryContract { /* ... */ }
```

Los `Services` reciben los contratos (interfaces), no las implementaciones concretas:
```kotlin
class AuthService(
    private val repository: UserRepositoryContract,  // ← Interfaz, no UserRepository
    private val tokenService: TokenService
)

class ProgramService(
    private val repository: ProgramRepositoryContract  // ← Interfaz, no ProgramRepository
)
```

---

## 🧹 Mappers (Funciones de Extensión de Kotlin)

Los mappers convierten entre las filas de la base de datos y los modelos de dominio/DTOs usando **funciones de extensión**:

```kotlin
// ResultRow (DB) → Domain Model
fun ResultRow.toDomainUser(): User
fun ResultRow.toDomainProgram(): Program

// Domain Model → DTO (Response)
fun User.toDto(): UserDto
fun Program.toDto(): ProgramDto
```

---

## ⚠️ Control de Errores y Excepciones

### Excepciones Personalizadas

| Excepción | Código HTTP | Uso |
|-----------|-------------|-----|
| `ConflictException` | `409 Conflict` | Email ya registrado en `/auth/register` |
| `UnauthorizedException` | `401 Unauthorized` | Credenciales inválidas en `/auth/login` |
| `NotFoundException` | `404 Not Found` | Programa no encontrado en GET/PUT/DELETE |

Definidas en `service/Exceptions.kt`:
```kotlin
class ConflictException(message: String) : Exception(message)
class UnauthorizedException(message: String) : Exception(message)
class NotFoundException(message: String) : Exception(message)
```

---

## 🛠️ Stack Tecnológico

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Kotlin | 2.1.20 | Lenguaje principal |
| Ktor | 3.1.3 | Framework HTTP del servidor |
| Exposed | 0.61.0 | ORM para PostgreSQL |
| PostgreSQL (Neon) | — | Base de datos en la nube |
| HikariCP | — | Pool de conexiones JDBC |
| BCrypt (`at.favre.lib`) | — | Hashing de contraseñas |
| java-jwt (`com.auth0`) | — | Generación y verificación de JWT |
| Kotlinx Serialization | — | Serialización JSON de DTOs |
| Logback | 1.4.14 | Logging |

---

## 🚀 Cómo Ejecutar

1. Clonar el repositorio
2. Abrir el proyecto en **Android Studio** o **IntelliJ IDEA**
3. Configurar las credenciales de Neon en `backend/src/main/resources/application.conf`
4. Ejecutar `Application.kt` (`fun main`)
5. El servidor inicia en `http://localhost:8080`
6. Las tablas se crean automáticamente en Neon al iniciar (`SchemaUtils.create`)

---

## 📝 Autor

Proyecto desarrollado como segunda entrega para la asignatura de Desarrollo Backend.

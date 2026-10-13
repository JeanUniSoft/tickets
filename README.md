# Gestor de Tickets — API REST con Spring Boot y JWT

API REST de una mesa de ayuda: los usuarios reportan problemas (tickets), cada ticket pertenece a una categoría, un administrador cambia su estado y cualquiera con sesión puede comentar. Proyecto del curso **Desarrollo Web Integrado** (UTP).

La API está versionada (`/api/v1`), protegida con tokens JWT, con los usuarios y roles guardados en base de datos, migraciones con Flyway, auditoría de fechas, paginación, bloqueo optimista y pruebas unitarias y de integración.

## Tecnologías

| Qué | Versión |
|---|---|
| Java | 25 |
| Spring Boot (Web MVC, Data JPA, Security, Validation) | 4.1.1 |
| Gradle (wrapper incluido) | 9.7.1 |
| JJWT | 0.12.6 |
| PostgreSQL (Neon) + Flyway | — |
| JUnit 5, Mockito, MockMvc, H2 (solo pruebas) | — |

## Operaciones REST

Todas las rutas van bajo el prefijo `/api/v1`.

| # | Método y ruta | Controlador | Quién puede | Qué hace |
|---|---|---|---|---|
| 1 | `POST /auth/login` | `AuthController` | Cualquiera | Comprueba credenciales y entrega un token |
| 2 | `POST /users` | `UserController` | Solo ADMIN | Crea un usuario con su rol |
| 3 | `GET /categories` | `CategoryController` | Con token | Lista las categorías |
| 4 | `POST /categories` | `CategoryController` | Solo ADMIN | Crea una categoría |
| 5 | `GET /tickets` | `TicketController` | Con token | Lista paginada (`?status=&page=&size=&sort=`) |
| 6 | `GET /tickets/{id}` | `TicketController` | Con token | Devuelve un ticket |
| 7 | `POST /tickets` | `TicketController` | Con token | Crea un ticket a nombre del dueño del token |
| 8 | `PUT /tickets/{id}/status` | `TicketController` | Solo ADMIN | Cambia el estado (con control de versión) |
| 9 | `GET /tickets/{ticketId}/comments` | `CommentController` | Con token | Lista los comentarios de un ticket |
| 10 | `POST /tickets/{ticketId}/comments` | `CommentController` | Con token | Agrega un comentario |

### Objetos que se transmiten

| Objeto | Atributos |
|---|---|
| Usuario | `id`, `username`, `fullName`, `email`, `roles` |
| Categoría | `id`, `name`, `description`, `active` |
| Ticket | `id`, `title`, `description`, `status`, `priority`, `category`, `creator`, `version`, `createdAt`, `updatedAt` |
| Comentario | `id`, `content`, `author`, `ticketId`, `createdAt` |

### Códigos de respuesta

| Código | Significa |
|---|---|
| 200 / 201 | Correcto / creado |
| 400 | Datos inválidos (validación, enum desconocido, orden no permitido) |
| 401 | Sin token, token inválido o login incorrecto |
| 403 | Con token, pero sin el rol necesario |
| 404 | El recurso no existe |
| 409 | Ya existe, o cambió desde que se leyó (bloqueo optimista) |

Los errores usan el formato estándar `ProblemDetail` (RFC 9457): `status`, `title`, `detail`, `instance`.

## Seguridad

- **JWT**: `POST /auth/login` cambia usuario y contraseña por un token firmado (HS256) que dura 60 minutos. Cada petición lo envía en la cabecera `Authorization: Bearer <token>` y `JwtAuthenticationFilter` lo verifica.
- **Usuarios desde base de datos**: tablas `users`, `roles` y `user_roles`; `CustomUserDetailsService` los carga en cada petición.
- **Contraseñas con BCrypt**: nunca se guarda ni se devuelve la contraseña, solo su hash.
- **Roles**: `@PreAuthorize("hasRole('ADMIN')")` protege las operaciones de administrador.
- **Sin sesiones** (`STATELESS`) y con los secretos fuera del código (archivo `.env`, que no se sube a git).

## Arquitectura

```
Cliente (curl, Postman, frontend)
   │  HTTP + JSON
   ▼
Filtros de Spring Security        security/JwtAuthenticationFilter, SecurityConfig
   ▼
Controlador                       controller/*Controller   (solo HTTP)
   ▼
Servicio                          service/*Service         (reglas y transacciones)
   ▼
Repositorio                       repository/*Repository   (Java → SQL)
   ▼
Entidad  ⇄  tabla en PostgreSQL   model/*  ⇄  db/migration/V*.sql (Flyway)

Errores: cualquier capa lanza una excepción → exception/GlobalExceptionHandler la convierte en JSON
```

```
src/main/java/pe/edu/utp/tickets/
├── config/       ApiPaths, JpaAuditingConfig
├── controller/   Auth, User, Category, Ticket, Comment
├── dto/          *Request (entra) y *Response (sale), PageResponse
├── exception/    excepciones del dominio y GlobalExceptionHandler
├── model/        entidades y enums
├── repository/   interfaces de Spring Data JPA
├── security/     JwtService, JwtAuthenticationFilter, SecurityConfig, ...
└── service/      reglas del negocio
src/main/resources/db/migration/
├── V1__create_schema.sql        tablas e índices
└── V2__insert_seed_data.sql     roles, 2 usuarios y 3 categorías
```

## Cómo ejecutarlo

Requisitos: JDK 25 y una base PostgreSQL vacía (por ejemplo, en [Neon](https://neon.tech)).

1. Crea la base de datos:

   ```sql
   CREATE DATABASE tickets;
   ```

2. Copia la plantilla de secretos y complétala:

   ```bash
   cp .env.example .env
   openssl rand -base64 48   # pega el resultado en JWT_SECRET
   ```

   ```properties
   DB_URL=jdbc:postgresql://TU_HOST/tickets?sslmode=require
   DB_USERNAME=TU_USUARIO
   DB_PASSWORD=TU_PASSWORD
   JWT_SECRET=LO_QUE_IMPRIMIO_OPENSSL
   ```

3. Arranca la aplicación:

   ```bash
   ./gradlew bootRun
   ```

   En el primer arranque Flyway crea las tablas y carga los datos de demostración. La API queda en `http://localhost:8080/api/v1`.

> Arráncala con `./gradlew bootRun`. Si la ejecutas desde el botón *Run* de un IDE que no compile con el flag `-parameters`, las rutas con `@PathVariable` o `@RequestParam` responden 500.

### Usuarios de demostración

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `ana` | `ana123` | USER |

Son datos de ejemplo de la migración `V2`: cámbialos antes de publicar la API.

## Probar a mano

```bash
API=http://localhost:8080/api/v1
JSON='Content-Type: application/json'

# Iniciar sesión y guardar el token
TOKEN=$(curl -s -X POST $API/auth/login -H "$JSON" \
  -d '{"username":"admin","password":"admin123"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')

# Crear un ticket
curl -s -X POST $API/tickets -H "Authorization: Bearer $TOKEN" -H "$JSON" \
  -d '{"title":"La PC no enciende","description":"Presiono el botón y no pasa nada","priority":"HIGH","categoryId":1}'

# Listar con paginación, filtro y orden
curl -s "$API/tickets?status=OPEN&page=0&size=5&sort=createdAt,desc" -H "Authorization: Bearer $TOKEN"

# Cambiar el estado enviando la versión que se leyó (bloqueo optimista)
curl -s -X PUT $API/tickets/1/status -H "Authorization: Bearer $TOKEN" -H "$JSON" \
  -d '{"status":"IN_PROGRESS","version":0}'

# Comentar
curl -s -X POST $API/tickets/1/comments -H "Authorization: Bearer $TOKEN" -H "$JSON" \
  -d '{"content":"Revisaremos la fuente de poder"}'
```

Repetir el `PUT` con la misma `version` responde **409**: otra persona (tú mismo, un momento antes) ya modificó el ticket.

## Pruebas

```bash
./gradlew cleanTest test
```

Son **31 pruebas**: 9 unitarias (servicios con Mockito, sin levantar Spring), 21 de integración (aplicación completa con MockMvc y H2 en memoria) y `contextLoads`. No necesitan la aplicación corriendo ni conexión a la base real.

```bash
./gradlew test --tests '*ServiceTest'      # solo las unitarias
open build/reports/tests/test/index.html   # informe web
```

### Casos de prueba de integración (2 por servicio REST)

| ID | Servicio | Entrada | Salida esperada | Verificación |
|---|---|---|---|---|
| CP-01 | `POST /auth/login` | `admin` / `admin123` | 200 y un token | El dueño del token es `admin` |
| CP-02 | `POST /auth/login` | `admin` / contraseña incorrecta | 401 | `detail` lo explica; no hay token |
| CP-03 | `POST /users` | Token de admin + datos de `luis` | 201 y el usuario | Sin contraseña en la respuesta; hash BCrypt en la base |
| CP-04 | `POST /users` | Token de ana + los mismos datos | 403 | `luis` no existe en la base |
| CP-05 | `GET /categories` | Token de ana | 200 y la lista | 1 categoría con sus 4 atributos |
| CP-06 | `GET /categories` | Sin token | 401 | El código es 401 |
| CP-07 | `POST /categories` | Token de admin + "Redes" | 201 y la categoría | `active` es `true`; la base tiene 2 |
| CP-08 | `POST /categories` | Token de admin + nombre vacío | 400 | `errors.name` existe; la base sigue con 1 |
| CP-09 | `GET /tickets` | Token de ana | 200 y la página | 1 elemento, página 0, tamaño 20, total 1 |
| CP-10 | `GET /tickets?status=CLOSED` | Token de ana + filtro | 200 y página vacía | 0 elementos, total 0 |
| CP-11 | `GET /tickets/{id}` | Token de ana + id existente | 200 y el ticket | Sus 10 atributos |
| CP-12 | `GET /tickets/{id}` | Token de ana + id inexistente | 404 | `detail` nombra el id |
| CP-13 | `POST /tickets` | Token de ana + ticket válido | 201 y el ticket | `OPEN`, versión 0, creador `ana`; la base tiene 2 |
| CP-14 | `POST /tickets` | Token de ana + título vacío | 400 | `errors.title` existe; la base sigue con 1 |
| CP-15 | `PUT /tickets/{id}/status` | Token de admin + `IN_PROGRESS` + versión 0 | 200 y el ticket | Versión 1; en la base el estado es `IN_PROGRESS` |
| CP-16 | `PUT /tickets/{id}/status` | Token de ana + `CLOSED` | 403 | En la base sigue `OPEN` |
| CP-17 | `GET /tickets/{ticketId}/comments` | Token de admin + ticket con 1 comentario | 200 y la lista | 1 comentario con sus 5 atributos |
| CP-18 | `GET /tickets/{ticketId}/comments` | Token de admin + id inexistente | 404 | El código es 404 |
| CP-19 | `POST /tickets/{ticketId}/comments` | Token de admin + texto | 201 y el comentario | Autor `admin`; 1 comentario en la base |
| CP-20 | `POST /tickets/{ticketId}/comments` | Token de admin + texto vacío | 400 | No hay comentarios en la base |
| CP-21 | `PUT /tickets/{id}/status` | Token de admin + `CLOSED` + versión 99 | 409 | En la base sigue `OPEN` |

### Casos de prueba unitarios

| ID | Clase y método | Condición | Resultado esperado |
|---|---|---|---|
| UT-01 | `CategoryService.create` | El nombre no existe | Guarda y devuelve la categoría activa |
| UT-02 | `CategoryService.create` | El nombre ya existe | `DuplicateResourceException`; no guarda |
| UT-03 | `UserService.create` | Usuario nuevo | Lo que se guarda es el hash, no la contraseña |
| UT-04 | `UserService.create` | El username ya existe | `DuplicateResourceException`; no guarda |
| UT-05 | `TicketService.create` | Datos válidos | Ticket `OPEN` a nombre del usuario recibido |
| UT-06 | `TicketService.create` | La categoría no existe | `InvalidRequestException`; no guarda |
| UT-07 | `TicketService.changeStatus` | La versión coincide | Cambia el estado y guarda |
| UT-08 | `TicketService.changeStatus` | La versión es antigua | `ConflictException`; el estado no cambia |
| UT-09 | `TicketService.findAll` | Orden por un atributo no permitido | `InvalidRequestException`; no consulta la base |

## Modelo de datos

```
roles ──< user_roles >── users ──────────────┐
                            │                │
                            │ crea           │ escribe
                            ▼                ▼
       categories ──<    tickets    ──<   comments
```

`A ──< B` significa "un A tiene muchos B".

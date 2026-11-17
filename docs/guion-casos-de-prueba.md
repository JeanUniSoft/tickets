# Guion de los casos de prueba (CP)

Uno presenta pantalla y otro lee. Cuando el profesor pregunte por un caso, se busca por su número y se lee lo que está entre comillas.

- **Abrir:** el archivo y el método que debe mostrar quien presenta pantalla.
- **Código línea por línea:** el código de la prueba y, al lado, qué hace cada línea.
- **Leer:** lo que dice en voz alta quien tiene el guion.
- **Si repregunta:** una respuesta corta para la duda más probable.

Primero están los 16 casos que pusimos en el Excel, en el mismo orden. Al final están los otros 5 (login, usuarios y el conflicto de versión).

## Índice

**Los 16 del Excel**

| Operación | Caso 1 | Caso 2 |
|---|---|---|
| 1. `GET /categories` | [CP-05](#cp-05) | [CP-06](#cp-06) |
| 2. `POST /categories` | [CP-07](#cp-07) | [CP-08](#cp-08) |
| 3. `GET /tickets` | [CP-09](#cp-09) | [CP-10](#cp-10) |
| 4. `GET /tickets/{id}` | [CP-11](#cp-11) | [CP-12](#cp-12) |
| 5. `POST /tickets` | [CP-13](#cp-13) | [CP-14](#cp-14) |
| 6. `PUT /tickets/{id}/status` | [CP-15](#cp-15) | [CP-16](#cp-16) |
| 7. `GET /tickets/{ticketId}/comments` | [CP-17](#cp-17) | [CP-18](#cp-18) |
| 8. `POST /tickets/{ticketId}/comments` | [CP-19](#cp-19) | [CP-20](#cp-20) |

**Los otros 5:** [CP-01](#cp-01) · [CP-02](#cp-02) · [CP-03](#cp-03) · [CP-04](#cp-04) · [CP-21](#cp-21)

---

## Lo que vale para todos los casos

Leer esto una sola vez, si el profesor pregunta cómo funcionan las pruebas en general.

**Abrir:** `BaseIntegrationTest`.

"Todos los casos de prueba heredan de esta clase. Levanta la aplicación completa y usa una base de datos H2 en memoria, así que nunca tocamos la base real."

"Antes de cada caso se ejecuta el método `setUpData`. Borra todas las tablas y vuelve a crear siempre lo mismo: el usuario admin, que es administrador; la usuaria Ana, que es usuaria normal; la categoría Hardware; y un ticket de Ana que se llama 'La PC no enciende', con prioridad alta y en estado abierto."

"Por eso cada caso empieza con los mismos datos y ninguno depende de otro."

"Para mandar las peticiones usamos MockMvc, que simula la petición HTTP sin abrir un puerto, pero pasando por toda la aplicación: la seguridad, el controlador, el servicio y la base."

"Y el método `bearer` genera un token real para el usuario que le indiquemos. Así probamos con seguridad de verdad."

Palabras que se repiten en todos los casos:

- `mockMvc.perform(...)`: manda la petición.
- `.header("Authorization", bearer("ana"))`: le pone el token de Ana.
- `.content(...)`: el JSON que se envía.
- `.andExpect(status()...)`: el código de respuesta que esperamos.
- `.andExpect(jsonPath("$.campo")...)`: revisa un campo del JSON de la respuesta.
- `assertThat(...)`: comprueba algo directamente en la base de datos.

Las dos anotaciones que están encima de cada caso:

| Código | Qué hace |
|---|---|
| `@Test` | Marca el método como una prueba: JUnit lo ejecuta al correr `./gradlew test`. |
| `@DisplayName("CP-10 ...")` | El nombre legible que aparece en el reporte de pruebas. |
| `class TicketControllerTest extends BaseIntegrationTest` | La clase hereda la aplicación levantada, `mockMvc`, los repositorios y los datos de prueba (`ana`, `hardware`, `ticket`). |

En cada caso, la tabla "Código línea por línea" tiene el código real de la prueba a la izquierda y qué hace esa línea a la derecha.

---

# Los 16 casos del Excel

## Operación 1 — `GET /categories`

<a id="cp-05"></a>
### CP-05 GET /categories con token devuelve 200 y la lista

**Abrir:** `CategoryControllerTest` → `findAll_withToken_returnsList`

| | |
|---|---|
| Entrada | Token de Ana |
| Salida esperada | 200 y la lista de categorías |
| Verificación | Hay 1 categoría y trae sus 4 atributos |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void findAll_withToken_returnsList() throws Exception {` | Nombre del método: qué se prueba, en qué condición y qué se espera. `throws Exception` porque `perform` puede lanzar errores. |
| 2 | `mockMvc.perform(get(API + "/categories")` | Envía un `GET` a `/api/v1/categories`. `API` es la constante con el prefijo `/api/v1`. |
| 3 | `.header("Authorization", bearer("ana"))` | Agrega la cabecera `Authorization` con un token JWT real de ana, generado por `JwtService`. |
| 4 | `.andExpect(status().isOk())` | Salida esperada: el código HTTP debe ser 200. |
| 5 | `.andExpect(jsonPath("$.length()").value(1))` | `$` es la raíz del JSON, que aquí es una lista. Debe tener exactamente 1 elemento. |
| 6 | `.andExpect(jsonPath("$[0].id").value(asInt(hardware.getId())))` | El `id` del primer elemento debe ser el de la categoría Hardware. `asInt` convierte el `Long` de Java al entero que viene en el JSON. |
| 7 | `.andExpect(jsonPath("$[0].name").value("Hardware"))` | El atributo `name` debe ser "Hardware". |
| 8 | `.andExpect(jsonPath("$[0].description").value("Equipos y periféricos"))` | El atributo `description` debe ser el que se guardó. |
| 9 | `.andExpect(jsonPath("$[0].active").value(true));` | El atributo `active` debe ser `true`. Con esto quedan comprobados los 4 atributos. |
| 10 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Este caso prueba que un usuario con token puede ver las categorías. Mandamos un GET a categorías con el token de Ana."

"Esperamos un 200. Después revisamos el contenido: que la lista tenga un solo elemento, que es la categoría Hardware que se crea antes de cada prueba."

"Y comprobamos sus cuatro atributos uno por uno: el id, el nombre 'Hardware', la descripción 'Equipos y periféricos' y que esté activa."

**Si repregunta — ¿por qué esperan exactamente una?** "Porque antes de cada prueba la base se limpia y solo se crea la categoría Hardware."

<a id="cp-06"></a>
### CP-06 GET /categories sin token devuelve 401

**Abrir:** `CategoryControllerTest` → `findAll_withoutToken_returns401`

| | |
|---|---|
| Entrada | Ninguna cabecera Authorization |
| Salida esperada | 401 |
| Verificación | El código es 401 |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void findAll_withoutToken_returns401() throws Exception {` | Nombre del método: listar sin token debe dar 401. |
| 2 | `mockMvc.perform(get(API + "/categories"))` | Envía el mismo `GET`, pero sin la línea `.header(...)`: la petición va sin token. |
| 3 | `.andExpect(status().isUnauthorized());` | Salida esperada y verificación: el código debe ser 401 (no autenticado). |
| 4 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Este es el caso contrario al anterior. Mandamos el mismo GET a categorías, pero sin token. Fíjense que aquí no hay ninguna línea de `header`."

"Esperamos un 401, que significa 'no autenticado'. Con esto probamos que la API está protegida: sin token no se entra."

**Si repregunta — ¿quién responde el 401?** "La configuración de seguridad. Como el filtro no encontró token, nadie quedó autenticado, y la regla dice que todas las rutas menos el login piden estar autenticado."

## Operación 2 — `POST /categories`

<a id="cp-07"></a>
### CP-07 POST /categories como ADMIN crea la categoría y devuelve 201

**Abrir:** `CategoryControllerTest` → `create_asAdmin_returns201`

| | |
|---|---|
| Entrada | Token de admin + categoría "Redes" |
| Salida esperada | 201 y la categoría creada, activa |
| Verificación | La respuesta trae id y en la base ahora hay 2 categorías |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void create_asAdmin_returns201() throws Exception {` | Nombre del método: crear como ADMIN debe dar 201. |
| 2 | `mockMvc.perform(post(API + "/categories")` | Envía un `POST` a `/api/v1/categories`. |
| 3 | `.header("Authorization", bearer("admin"))` | Agrega la cabecera `Authorization` con un token JWT real de admin, generado por `JwtService`. |
| 4 | `.contentType(MediaType.APPLICATION_JSON)` | Indica que el cuerpo de la petición es JSON (cabecera `Content-Type`). |
| 5 | `.content("""` | Abre el cuerpo de la petición. Las tres comillas son un bloque de texto de Java: permite escribir el JSON sin escapar comillas. |
| 6 | `{"name": "Redes", "description": "Internet, wifi y servidores"}` | El JSON que se envía. Spring lo convierte en un `CategoryRequest`. |
| 7 | `"""))` | Cierra el bloque de texto, el `content` y el `perform`. |
| 8 | `.andExpect(status().isCreated())` | Salida esperada: código 201 (creado). |
| 9 | `.andExpect(jsonPath("$.id").isNumber())` | La respuesta debe traer un `id` numérico: la base ya se lo asignó. |
| 10 | `.andExpect(jsonPath("$.name").value("Redes"))` | El nombre devuelto debe ser el que se envió. |
| 11 | `.andExpect(jsonPath("$.active").value(true));` | La categoría nace activa aunque el cliente no lo mande. |
| 12 | `assertThat(categoryRepository.count()).isEqualTo(2);` | Verificación en la base: cuenta las filas de categorías. Deben ser 2: Hardware más Redes. |
| 13 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Aquí probamos que un administrador puede crear una categoría. Mandamos un POST con el token de admin y un JSON con el nombre 'Redes' y su descripción."

"Esperamos un 201, que significa 'creado'. En la respuesta revisamos que venga un id numérico, que el nombre sea 'Redes' y que esté activa."

"Y la última línea va a la base de datos y cuenta las categorías. Esperamos dos: Hardware, que ya estaba, y Redes, que acabamos de crear. Así confirmamos que realmente se guardó."

**Si repregunta — ¿por qué 'active' es verdadero si no lo mandaron?** "Porque toda categoría nace activa. Ese valor lo pone el sistema, no el cliente."

<a id="cp-08"></a>
### CP-08 POST /categories con nombre vacío devuelve 400

**Abrir:** `CategoryControllerTest` → `create_withBlankName_returns400`

| | |
|---|---|
| Entrada | Token de admin + categoría con el nombre vacío |
| Salida esperada | 400 y el campo que falló dentro de "errors" |
| Verificación | `errors.name` existe y en la base sigue habiendo 1 categoría |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void create_withBlankName_returns400() throws Exception {` | Nombre del método: crear con el nombre en blanco debe dar 400. |
| 2 | `mockMvc.perform(post(API + "/categories")` | Envía un `POST` a `/api/v1/categories`. |
| 3 | `.header("Authorization", bearer("admin"))` | Agrega la cabecera `Authorization` con un token JWT real de admin, generado por `JwtService`. |
| 4 | `.contentType(MediaType.APPLICATION_JSON)` | Indica que el cuerpo de la petición es JSON (cabecera `Content-Type`). |
| 5 | `.content("""` | Abre el cuerpo de la petición. |
| 6 | `{"name": "", "description": "Sin nombre"}` | JSON con el nombre vacío: rompe la regla `@NotBlank` de `CategoryRequest`. |
| 7 | `"""))` | Cierra el cuerpo y el `perform`. |
| 8 | `.andExpect(status().isBadRequest())` | Salida esperada: código 400 (datos inválidos). |
| 9 | `.andExpect(jsonPath("$.errors.name").exists());` | Dentro de `errors` debe existir la clave `name`: la API dice qué campo falló. |
| 10 | `assertThat(categoryRepository.count()).isEqualTo(1);` | Verificación en la base: sigue habiendo 1 sola categoría, no se guardó nada. |
| 11 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Este caso prueba la validación. Mandamos el mismo POST con el token de admin, pero con el nombre vacío."

"Esperamos un 400, que significa 'datos inválidos'. Y revisamos que en la respuesta exista `errors.name`, o sea, que la API nos diga exactamente qué campo está mal."

"Al final contamos las categorías en la base y esperamos una sola. Eso confirma que no se guardó nada."

**Si repregunta — ¿dónde está esa regla?** "En `CategoryRequest`, el nombre tiene la anotación `@NotBlank`. Y en el controlador, el `@Valid` hace que se revise antes de ejecutar el método."

## Operación 3 — `GET /tickets`

<a id="cp-09"></a>
### CP-09 GET /tickets devuelve 200 y la primera página

**Abrir:** `TicketControllerTest` → `findAll_withoutFilters_returnsFirstPage`

| | |
|---|---|
| Entrada | Token de Ana, sin filtros ni parámetros de página |
| Salida esperada | 200 y la primera página con los valores por defecto |
| Verificación | 1 elemento, página 0, tamaño 20, total 1 |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void findAll_withoutFilters_returnsFirstPage() throws Exception {` | Nombre del método: listar sin filtros devuelve la primera página. |
| 2 | `mockMvc.perform(get(API + "/tickets").header("Authorization", bearer("ana")))` | Envía un `GET` a `/api/v1/tickets` con el token de ana. No lleva parámetros: se usan los valores por defecto. |
| 3 | `.andExpect(status().isOk())` | Salida esperada: código 200. |
| 4 | `.andExpect(jsonPath("$.content.length()").value(1))` | `content` es la lista de tickets de la página. Debe tener 1 elemento. |
| 5 | `.andExpect(jsonPath("$.content[0].id").value(asInt(ticket.getId())))` | Ese elemento debe ser el ticket de prueba (mismo `id`). |
| 6 | `.andExpect(jsonPath("$.content[0].title").value("La PC no enciende"))` | Y su título debe ser el que se guardó. |
| 7 | `.andExpect(jsonPath("$.page").value(0))` | Número de página: 0, la primera. |
| 8 | `.andExpect(jsonPath("$.size").value(20))` | Tamaño de página: 20, el valor de `@PageableDefault` en el controlador. |
| 9 | `.andExpect(jsonPath("$.totalElements").value(1));` | Total de tickets en la base: 1. |
| 10 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Este caso prueba el listado de tickets. Mandamos un GET a tickets con el token de Ana, sin ningún filtro."

"Esperamos un 200. La respuesta viene paginada, así que revisamos varias cosas. Dentro de `content` debe haber un solo ticket, con el id y el título 'La PC no enciende', que es el ticket de prueba."

"Y revisamos los datos de la página: que sea la página cero, que el tamaño sea veinte y que el total de elementos sea uno. Esos son los valores por defecto cuando el cliente no manda nada."

**Si repregunta — ¿por qué la página es cero?** "Es la convención de Spring: la primera página es la cero."

<a id="cp-10"></a>
### CP-10 GET /tickets?status=CLOSED devuelve 200 y una página vacía

**Abrir:** `TicketControllerTest` → `findAll_filteringByStatus_returnsEmptyPage`

| | |
|---|---|
| Entrada | Token de Ana + filtro `status=CLOSED` |
| Salida esperada | 200 y una página vacía |
| Verificación | 0 elementos y total 0 |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void findAll_filteringByStatus_returnsEmptyPage() throws Exception {` | Nombre del método: filtrar por estado devuelve una página vacía. |
| 2 | `mockMvc.perform(get(API + "/tickets").param("status", "CLOSED").header("Authorization", bearer("ana")))` | Envía `GET /api/v1/tickets?status=CLOSED` con el token de ana. `.param` agrega el parámetro a la URL; el controlador lo recibe con `@RequestParam`. |
| 3 | `.andExpect(status().isOk())` | Salida esperada: código 200. Una búsqueda sin resultados no es un error. |
| 4 | `.andExpect(jsonPath("$.content.length()").value(0))` | La lista `content` debe venir vacía: el único ticket está OPEN. |
| 5 | `.andExpect(jsonPath("$.totalElements").value(0));` | El total de tickets que cumplen el filtro es 0. |
| 6 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Este caso prueba el filtro por estado. Mandamos el mismo GET a tickets con el token de Ana, pero agregamos el parámetro `status` con el valor CLOSED. Eso es la línea que dice `.param`."

"El único ticket que hay en la base está abierto, así que ninguno cumple el filtro."

"Esperamos un 200, no un error. Y revisamos que `content` tenga cero elementos y que el total sea cero."

"Lo que demuestra este caso es que el filtro funciona de verdad: si no filtrara, nos devolvería el ticket abierto."

**Si repregunta — ¿por qué 200 y no 404?** "Porque la búsqueda sí funcionó, solo que no encontró nada. Una lista vacía es una respuesta correcta. El 404 lo usamos cuando se pide un ticket específico que no existe."

## Operación 4 — `GET /tickets/{id}`

<a id="cp-11"></a>
### CP-11 GET /tickets/{id} de un ticket que existe devuelve 200 y sus datos

**Abrir:** `TicketControllerTest` → `findById_whenTicketExists_returnsTicket`

| | |
|---|---|
| Entrada | Token de Ana + el id de un ticket que existe |
| Salida esperada | 200 y el ticket completo |
| Verificación | Llegan sus 10 atributos con los valores guardados |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void findById_whenTicketExists_returnsTicket() throws Exception {` | Nombre del método: buscar un ticket que existe devuelve el ticket. |
| 2 | `mockMvc.perform(get(API + "/tickets/{id}", ticket.getId()).header("Authorization", bearer("ana")))` | Envía `GET /api/v1/tickets/{id}`. `{id}` se reemplaza por el id del ticket de prueba; el controlador lo recibe con `@PathVariable`. Lleva el token de ana. |
| 3 | `.andExpect(status().isOk())` | Salida esperada: código 200. |
| 4 | `.andExpect(jsonPath("$.id").value(asInt(ticket.getId())))` | Atributo 1: `id`, el mismo que se pidió. |
| 5 | `.andExpect(jsonPath("$.title").value("La PC no enciende"))` | Atributo 2: `title`. |
| 6 | `.andExpect(jsonPath("$.description").value("Presiono el botón y no pasa nada"))` | Atributo 3: `description`. |
| 7 | `.andExpect(jsonPath("$.status").value("OPEN"))` | Atributo 4: `status`. El enum viaja como texto. |
| 8 | `.andExpect(jsonPath("$.priority").value("HIGH"))` | Atributo 5: `priority`. |
| 9 | `.andExpect(jsonPath("$.category").value("Hardware"))` | Atributo 6: `category`, el nombre de la categoría. |
| 10 | `.andExpect(jsonPath("$.creator").value("ana"))` | Atributo 7: `creator`, el nombre de usuario de quien lo creó. |
| 11 | `.andExpect(jsonPath("$.version").value(0))` | Atributo 8: `version`. Es 0 porque nunca se modificó. |
| 12 | `.andExpect(jsonPath("$.createdAt").isNotEmpty())` | Atributo 9: `createdAt`. Solo se pide que exista, porque la fecha cambia en cada ejecución. |
| 13 | `.andExpect(jsonPath("$.updatedAt").isNotEmpty());` | Atributo 10: `updatedAt`, igual que el anterior. |
| 14 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Este caso prueba que se puede consultar un ticket por su id. Mandamos un GET con el id del ticket de prueba y el token de Ana."

"Esperamos un 200, y aquí revisamos los diez atributos del ticket: el id, el título, la descripción, el estado OPEN, la prioridad HIGH, la categoría Hardware, el creador Ana, la versión cero, y que las fechas de creación y de actualización no estén vacías."

"Este es el caso que mejor muestra que transmitimos un objeto completo."

**Si repregunta — ¿por qué las fechas solo se revisan con 'no vacío'?** "Porque las pone el sistema en el momento de guardar y cambian en cada ejecución. No podemos saber el valor exacto, solo que existan."

<a id="cp-12"></a>
### CP-12 GET /tickets/{id} de un ticket que no existe devuelve 404

**Abrir:** `TicketControllerTest` → `findById_whenTicketDoesNotExist_returns404`

| | |
|---|---|
| Entrada | Token de Ana + un id que no existe |
| Salida esperada | 404 con el motivo en "detail" |
| Verificación | El código es 404 y el mensaje nombra el id buscado |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void findById_whenTicketDoesNotExist_returns404() throws Exception {` | Nombre del método: buscar un ticket que no existe da 404. |
| 2 | `mockMvc.perform(get(API + "/tickets/{id}", NON_EXISTENT_ID).header("Authorization", bearer("ana")))` | Envía el `GET` con `NON_EXISTENT_ID` (999999), un id que no está en la base. Lleva el token de ana. |
| 3 | `.andExpect(status().isNotFound())` | Salida esperada: código 404 (no encontrado). |
| 4 | `.andExpect(jsonPath("$.detail").value("No existe el ticket " + NON_EXISTENT_ID));` | El campo `detail` debe traer el mensaje de la excepción `ResourceNotFoundException`, con el id buscado. |
| 5 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Aquí pedimos un ticket que no existe. Usamos la constante `NON_EXISTENT_ID`, que vale 999999, un id que nunca va a estar en la base."

"Esperamos un 404, que significa 'no encontrado'. Y revisamos que el campo `detail` diga 'No existe el ticket' seguido de ese número."

**Si repregunta — ¿quién arma ese mensaje?** "El servicio lanza una excepción `ResourceNotFoundException`, y `GlobalExceptionHandler` la convierte en una respuesta 404 con ese mensaje."

## Operación 5 — `POST /tickets`

<a id="cp-13"></a>
### CP-13 POST /tickets con datos válidos crea el ticket y devuelve 201

**Abrir:** `TicketControllerTest` → `create_withValidData_returns201`

| | |
|---|---|
| Entrada | Token de Ana + título, descripción, prioridad y categoría válidos |
| Salida esperada | 201 y el ticket creado |
| Verificación | Nace OPEN, con versión 0, su creador es Ana y la base tiene 2 tickets |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void create_withValidData_returns201() throws Exception {` | Nombre del método: crear con datos válidos da 201. |
| 2 | `mockMvc.perform(post(API + "/tickets")` | Envía un `POST` a `/api/v1/tickets`. |
| 3 | `.header("Authorization", bearer("ana"))` | Agrega la cabecera `Authorization` con un token JWT real de ana, generado por `JwtService`. |
| 4 | `.contentType(MediaType.APPLICATION_JSON)` | Indica que el cuerpo de la petición es JSON (cabecera `Content-Type`). |
| 5 | `.content("""` | Abre el cuerpo de la petición (bloque de texto de Java). |
| 6 | `{` | Empieza el JSON, que Spring convertirá en un `TicketRequest`. |
| 7 | `"title": "Sin internet en el aula B204",` | Título del ticket. |
| 8 | `"description": "Ninguna PC del aula tiene red",` | Descripción del problema. |
| 9 | `"priority": "MEDIUM",` | Prioridad. Debe ser un valor del enum `Priority`. |
| 10 | `"categoryId": %d` | Id de la categoría. `%d` es un hueco que se llena en la línea siguiente. |
| 11 | `}` | Termina el JSON. No lleva ni estado ni creador. |
| 12 | `""".formatted(hardware.getId())))` | Cierra el texto y reemplaza `%d` por el id real de la categoría Hardware. |
| 13 | `.andExpect(status().isCreated())` | Salida esperada: código 201 (creado). |
| 14 | `.andExpect(jsonPath("$.id").isNumber())` | La respuesta trae un `id` numérico asignado por la base. |
| 15 | `.andExpect(jsonPath("$.status").value("OPEN"))` | El ticket nace OPEN: lo fija el constructor de `Ticket`, no el cliente. |
| 16 | `.andExpect(jsonPath("$.priority").value("MEDIUM"))` | La prioridad es la que se envió. |
| 17 | `.andExpect(jsonPath("$.category").value("Hardware"))` | La categoría se resolvió a partir del `categoryId`. |
| 18 | `.andExpect(jsonPath("$.creator").value("ana"))` | El creador es el dueño del token, aunque no se envió en el JSON. |
| 19 | `.andExpect(jsonPath("$.version").value(0))` | Versión inicial 0, puesta por `@Version`. |
| 20 | `.andExpect(jsonPath("$.createdAt").isNotEmpty());` | La fecha de creación la puso el sistema (auditoría). |
| 21 | `assertThat(ticketRepository.count()).isEqualTo(2);` | Verificación en la base: ahora hay 2 tickets, el de prueba y el nuevo. |
| 22 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Este caso prueba la creación de un ticket. Mandamos un POST con el token de Ana y un JSON con cuatro datos: el título 'Sin internet en el aula B204', la descripción, la prioridad MEDIUM y el id de la categoría Hardware."

"Esperamos un 201. En la respuesta revisamos que venga un id, que el estado sea OPEN, la prioridad MEDIUM, la categoría Hardware, el creador Ana y la versión cero."

"Fíjense que en el JSON no mandamos ni el estado ni el creador. El estado lo pone el sistema, porque todo ticket nace abierto. Y el creador sale del token."

"La última línea cuenta los tickets en la base y espera dos: el de prueba más el nuevo."

**Si repregunta — ¿qué es ese `%d` y el `.formatted`?** "Es para meter dentro del JSON el id real de la categoría Hardware, que se genera en cada ejecución."

<a id="cp-14"></a>
### CP-14 POST /tickets sin título devuelve 400 y no guarda nada

**Abrir:** `TicketControllerTest` → `create_withBlankTitle_returns400`

| | |
|---|---|
| Entrada | Token de Ana + un ticket sin título |
| Salida esperada | 400 y el campo que falló dentro de "errors" |
| Verificación | `errors.title` existe y la base sigue con 1 ticket |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void create_withBlankTitle_returns400() throws Exception {` | Nombre del método: crear con el título en blanco da 400. |
| 2 | `mockMvc.perform(post(API + "/tickets")` | Envía un `POST` a `/api/v1/tickets`. |
| 3 | `.header("Authorization", bearer("ana"))` | Agrega la cabecera `Authorization` con un token JWT real de ana, generado por `JwtService`. |
| 4 | `.contentType(MediaType.APPLICATION_JSON)` | Indica que el cuerpo de la petición es JSON (cabecera `Content-Type`). |
| 5 | `.content("""` | Abre el cuerpo de la petición (bloque de texto de Java). |
| 6 | `{` | Empieza el JSON, que Spring convertirá en un `TicketRequest`. |
| 7 | `"title": "",` | Título vacío: rompe la regla `@NotBlank` de `TicketRequest`. |
| 8 | `"description": "Ninguna PC del aula tiene red",` | Descripción del problema. |
| 9 | `"priority": "MEDIUM",` | Prioridad. Debe ser un valor del enum `Priority`. |
| 10 | `"categoryId": %d` | Id de la categoría. `%d` es un hueco que se llena en la línea siguiente. |
| 11 | `}` | Termina el JSON. No lleva ni estado ni creador. |
| 12 | `""".formatted(hardware.getId())))` | Cierra el texto y reemplaza `%d` por el id real de la categoría Hardware. |
| 13 | `.andExpect(status().isBadRequest())` | Salida esperada: código 400. `@Valid` rechaza el objeto antes de entrar al controlador. |
| 14 | `.andExpect(jsonPath("$.errors.title").exists());` | Dentro de `errors` debe existir la clave `title`. |
| 15 | `assertThat(ticketRepository.count()).isEqualTo(1);` | Verificación en la base: sigue habiendo 1 ticket, no se guardó el inválido. |
| 16 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Este es el caso de error al crear un ticket. Es el mismo POST con el token de Ana, pero el título va vacío."

"Esperamos un 400. Y revisamos que en la respuesta exista `errors.title`, que es donde la API explica qué campo falló."

"Después contamos los tickets en la base y esperamos uno solo, el de prueba. Así confirmamos que el ticket inválido no se guardó."

**Si repregunta — ¿dónde se valida?** "En `TicketRequest`, el título tiene `@NotBlank`. La validación ocurre antes de entrar al controlador, por eso nunca llega al servicio ni a la base."

## Operación 6 — `PUT /tickets/{id}/status`

<a id="cp-15"></a>
### CP-15 PUT /tickets/{id}/status como ADMIN cambia el estado y devuelve 200

**Abrir:** `TicketControllerTest` → `changeStatus_asAdmin_returns200`

| | |
|---|---|
| Entrada | Token de admin + nuevo estado IN_PROGRESS + la versión actual (0) |
| Salida esperada | 200 y el ticket con el estado nuevo y la versión 1 |
| Verificación | Al volver a leer el ticket de la base, su estado es IN_PROGRESS |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void changeStatus_asAdmin_returns200() throws Exception {` | Nombre del método: cambiar el estado como ADMIN da 200. |
| 2 | `mockMvc.perform(put(API + "/tickets/{id}/status", ticket.getId())` | Envía un `PUT` a `/api/v1/tickets/{id}/status` con el id del ticket de prueba. |
| 3 | `.header("Authorization", bearer("admin"))` | Agrega la cabecera `Authorization` con un token JWT real de admin, generado por `JwtService`. |
| 4 | `.contentType(MediaType.APPLICATION_JSON)` | Indica que el cuerpo de la petición es JSON (cabecera `Content-Type`). |
| 5 | `.content("""` | Abre el cuerpo de la petición. |
| 6 | `{"status": "IN_PROGRESS", "version": 0}` | JSON que se convierte en `ChangeStatusRequest`: el estado nuevo y la versión que el cliente leyó (0, la actual). |
| 7 | `"""))` | Cierra el cuerpo y el `perform`. |
| 8 | `.andExpect(status().isOk())` | Salida esperada: código 200. |
| 9 | `.andExpect(jsonPath("$.id").value(asInt(ticket.getId())))` | La respuesta es el mismo ticket. |
| 10 | `.andExpect(jsonPath("$.status").value("IN_PROGRESS"))` | El estado ya es el nuevo. |
| 11 | `.andExpect(jsonPath("$.version").value(1));` | La versión subió de 0 a 1: Hibernate la incrementa en cada `UPDATE`. |
| 12 | `assertThat(ticketRepository.findById(ticket.getId()).orElseThrow().getStatus())` | Verificación en la base: vuelve a leer el ticket y toma su estado. `orElseThrow` falla si no existiera. |
| 13 | `.isEqualTo(TicketStatus.IN_PROGRESS);` | El estado guardado debe ser `IN_PROGRESS`. |
| 14 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Este caso prueba que un administrador puede cambiar el estado de un ticket. Mandamos un PUT con el token de admin y un JSON con dos datos: el nuevo estado, que es IN_PROGRESS, y la versión cero, que es la versión actual del ticket."

"Esperamos un 200. En la respuesta revisamos que el estado ya sea IN_PROGRESS y que la versión haya subido a uno."

"Y al final volvemos a leer el ticket directamente de la base y comprobamos que su estado es IN_PROGRESS. Eso asegura que el cambio se guardó de verdad y no solo aparece en la respuesta."

**Si repregunta — ¿para qué mandan la versión?** "Para evitar que dos personas se pisen. Si la versión que mando no es la actual, significa que alguien cambió el ticket antes que yo, y la API rechaza mi cambio."

<a id="cp-16"></a>
### CP-16 PUT /tickets/{id}/status como USER devuelve 403 y no cambia nada

**Abrir:** `TicketControllerTest` → `changeStatus_asRegularUser_returns403`

| | |
|---|---|
| Entrada | Token de Ana (ROLE_USER) + nuevo estado CLOSED |
| Salida esperada | 403, porque solo un ADMIN cambia estados |
| Verificación | El código es 403 y en la base el ticket sigue OPEN |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void changeStatus_asRegularUser_returns403() throws Exception {` | Nombre del método: cambiar el estado como usuario normal da 403. |
| 2 | `mockMvc.perform(put(API + "/tickets/{id}/status", ticket.getId())` | Envía un `PUT` a `/api/v1/tickets/{id}/status` con el id del ticket de prueba. |
| 3 | `.header("Authorization", bearer("ana"))` | Agrega la cabecera `Authorization` con un token JWT real de ana, generado por `JwtService`. |
| 4 | `.contentType(MediaType.APPLICATION_JSON)` | Indica que el cuerpo de la petición es JSON (cabecera `Content-Type`). |
| 5 | `.content("""` | Abre el cuerpo de la petición. |
| 6 | `{"status": "CLOSED", "version": 0}` | Petición correcta en su forma: lo único que falla es el rol de quien la envía. |
| 7 | `"""))` | Cierra el cuerpo y el `perform`. |
| 8 | `.andExpect(status().isForbidden());` | Salida esperada: código 403. `@PreAuthorize("hasRole('ADMIN')")` corta la petición antes de ejecutar el método. |
| 9 | `assertThat(ticketRepository.findById(ticket.getId()).orElseThrow().getStatus())` | Verificación en la base: vuelve a leer el ticket y toma su estado. |
| 10 | `.isEqualTo(TicketStatus.OPEN);` | El ticket debe seguir `OPEN`: no se modificó. |
| 11 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Este caso prueba los permisos por rol. Es el mismo PUT, pero con el token de Ana, que es una usuaria normal, intentando cerrar el ticket."

"Esperamos un 403, que significa 'prohibido'. Ana sí está autenticada, pero no tiene el rol de administrador."

"Después leemos el ticket de la base y comprobamos que sigue en OPEN. Esto es importante: no basta con que responda 403, también verificamos que el ticket no se modificó."

**Si repregunta — ¿dónde está esa regla?** "En `TicketController`, el método `changeStatus` tiene la anotación `@PreAuthorize("hasRole('ADMIN')")`."

**Si repregunta — ¿diferencia entre 401 y 403?** "401 es que no sabemos quién eres. 403 es que sabemos quién eres, pero no tienes permiso."

## Operación 7 — `GET /tickets/{ticketId}/comments`

<a id="cp-17"></a>
### CP-17 GET /tickets/{ticketId}/comments devuelve 200 y los comentarios del ticket

**Abrir:** `CommentControllerTest` → `findByTicket_whenTicketExists_returnsComments`

| | |
|---|---|
| Entrada | Token de admin + el id de un ticket que tiene 1 comentario de Ana |
| Salida esperada | 200 y la lista de comentarios |
| Verificación | Hay 1 comentario con sus 5 atributos |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void findByTicket_whenTicketExists_returnsComments() throws Exception {` | Nombre del método: listar comentarios de un ticket que existe. |
| 2 | `Comment comment = commentRepository.save(new Comment("Ya probé con otro cable", ticket, ana));` | Preparación: guarda directo en la base un comentario de ana sobre el ticket de prueba. Se guarda la referencia para comparar su id después. |
| 3 | `mockMvc.perform(get(API + "/tickets/{ticketId}/comments", ticket.getId())` | Envía `GET /api/v1/tickets/{ticketId}/comments` con el id del ticket. |
| 4 | `.header("Authorization", bearer("admin"))` | Agrega la cabecera `Authorization` con un token JWT real de admin, generado por `JwtService`. |
| 5 | `.andExpect(status().isOk())` | Salida esperada: código 200. |
| 6 | `.andExpect(jsonPath("$.length()").value(1))` | La lista debe tener 1 comentario. |
| 7 | `.andExpect(jsonPath("$[0].id").value(asInt(comment.getId())))` | Atributo 1: `id`, el del comentario que se guardó. |
| 8 | `.andExpect(jsonPath("$[0].content").value("Ya probé con otro cable"))` | Atributo 2: `content`. |
| 9 | `.andExpect(jsonPath("$[0].author").value("ana"))` | Atributo 3: `author`. |
| 10 | `.andExpect(jsonPath("$[0].ticketId").value(asInt(ticket.getId())))` | Atributo 4: `ticketId`, el ticket al que pertenece. |
| 11 | `.andExpect(jsonPath("$[0].createdAt").isNotEmpty());` | Atributo 5: `createdAt`. Solo se pide que exista. |
| 12 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Este caso prueba que se pueden listar los comentarios de un ticket. La primera línea prepara el dato: guarda en la base un comentario de Ana que dice 'Ya probé con otro cable'."

"Luego mandamos un GET a los comentarios de ese ticket con el token de admin."

"Esperamos un 200. Revisamos que la lista tenga un solo comentario y comprobamos sus cinco atributos: el id, el contenido, el autor Ana, el id del ticket y que la fecha de creación no esté vacía."

**Si repregunta — ¿por qué crean el comentario dentro de la prueba?** "Porque los datos que se crean antes de cada prueba no incluyen comentarios. Este caso necesita uno, así que lo crea él mismo."

<a id="cp-18"></a>
### CP-18 GET /tickets/{ticketId}/comments de un ticket que no existe devuelve 404

**Abrir:** `CommentControllerTest` → `findByTicket_whenTicketDoesNotExist_returns404`

| | |
|---|---|
| Entrada | Token de admin + el id de un ticket que no existe |
| Salida esperada | 404 |
| Verificación | El código es 404 |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void findByTicket_whenTicketDoesNotExist_returns404() throws Exception {` | Nombre del método: listar comentarios de un ticket que no existe da 404. |
| 2 | `mockMvc.perform(get(API + "/tickets/{ticketId}/comments", NON_EXISTENT_ID)` | Envía el `GET` con el id 999999, que no existe. |
| 3 | `.header("Authorization", bearer("admin"))` | Agrega la cabecera `Authorization` con un token JWT real de admin, generado por `JwtService`. |
| 4 | `.andExpect(status().isNotFound());` | Salida esperada y verificación: código 404. |
| 5 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Aquí pedimos los comentarios de un ticket que no existe, usando el id 999999, con el token de admin."

"Esperamos un 404. Lo que probamos es que la API revisa primero que el ticket exista. No devuelve una lista vacía, porque eso haría creer que el ticket existe y simplemente no tiene comentarios."

**Si repregunta — ¿por qué aquí 404 y en el CP-10 200?** "En el CP-10 la búsqueda es válida y no hay resultados. Aquí el ticket por el que preguntamos no existe, así que la ruta apunta a algo que no está."

## Operación 8 — `POST /tickets/{ticketId}/comments`

<a id="cp-19"></a>
### CP-19 POST /tickets/{ticketId}/comments agrega el comentario y devuelve 201

**Abrir:** `CommentControllerTest` → `create_withValidContent_returns201`

| | |
|---|---|
| Entrada | Token de admin + un comentario con texto |
| Salida esperada | 201 y el comentario creado |
| Verificación | El autor es el dueño del token y en la base hay 1 comentario |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void create_withValidContent_returns201() throws Exception {` | Nombre del método: comentar con texto válido da 201. |
| 2 | `mockMvc.perform(post(API + "/tickets/{ticketId}/comments", ticket.getId())` | Envía un `POST` a los comentarios del ticket de prueba. |
| 3 | `.header("Authorization", bearer("admin"))` | Agrega la cabecera `Authorization` con un token JWT real de admin, generado por `JwtService`. |
| 4 | `.contentType(MediaType.APPLICATION_JSON)` | Indica que el cuerpo de la petición es JSON (cabecera `Content-Type`). |
| 5 | `.content("""` | Abre el cuerpo de la petición. |
| 6 | `{"content": "Revisaremos la fuente de poder"}` | JSON que se convierte en `CommentRequest`. Solo lleva el texto; el autor no se envía. |
| 7 | `"""))` | Cierra el cuerpo y el `perform`. |
| 8 | `.andExpect(status().isCreated())` | Salida esperada: código 201. |
| 9 | `.andExpect(jsonPath("$.id").isNumber())` | La respuesta trae un `id` numérico. |
| 10 | `.andExpect(jsonPath("$.content").value("Revisaremos la fuente de poder"))` | El contenido es el que se envió. |
| 11 | `.andExpect(jsonPath("$.author").value("admin"))` | El autor es el dueño del token. |
| 12 | `.andExpect(jsonPath("$.ticketId").value(asInt(ticket.getId())));` | El comentario quedó asociado al ticket de la ruta. |
| 13 | `assertThat(commentRepository.count()).isEqualTo(1);` | Verificación en la base: hay 1 comentario guardado. |
| 14 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Este caso prueba que se puede comentar un ticket. Mandamos un POST con el token de admin y un JSON con el contenido 'Revisaremos la fuente de poder'."

"Esperamos un 201. En la respuesta revisamos que venga un id, que el contenido sea el que mandamos, que el autor sea admin y que el id del ticket sea el correcto."

"Fíjense que en el JSON solo mandamos el texto. El autor no se manda: sale del token."

"Al final contamos los comentarios en la base y esperamos uno."

**Si repregunta — ¿por qué el autor no va en el JSON?** "Para que nadie pueda comentar a nombre de otra persona. El sistema toma al autor del token, que es lo único confiable."

<a id="cp-20"></a>
### CP-20 POST /tickets/{ticketId}/comments con texto vacío devuelve 400

**Abrir:** `CommentControllerTest` → `create_withBlankContent_returns400`

| | |
|---|---|
| Entrada | Token de admin + un comentario con el texto vacío |
| Salida esperada | 400, la validación rechaza los datos |
| Verificación | El código es 400 y no hay comentarios en la base |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void create_withBlankContent_returns400() throws Exception {` | Nombre del método: comentar con el texto en blanco da 400. |
| 2 | `mockMvc.perform(post(API + "/tickets/{ticketId}/comments", ticket.getId())` | Envía un `POST` a los comentarios del ticket de prueba. |
| 3 | `.header("Authorization", bearer("admin"))` | Agrega la cabecera `Authorization` con un token JWT real de admin, generado por `JwtService`. |
| 4 | `.contentType(MediaType.APPLICATION_JSON)` | Indica que el cuerpo de la petición es JSON (cabecera `Content-Type`). |
| 5 | `.content("""` | Abre el cuerpo de la petición. |
| 6 | `{"content": ""}` | Texto vacío: rompe la regla `@NotBlank` de `CommentRequest`. |
| 7 | `"""))` | Cierra el cuerpo y el `perform`. |
| 8 | `.andExpect(status().isBadRequest());` | Salida esperada: código 400. |
| 9 | `assertThat(commentRepository.count()).isZero();` | Verificación en la base: no hay ningún comentario guardado. |
| 10 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Este es el caso de error al comentar. Es el mismo POST con el token de admin, pero el contenido va vacío."

"Esperamos un 400, porque un comentario sin texto no tiene sentido."

"Y comprobamos que en la base no hay ningún comentario, con `isZero`. Así confirmamos que no se guardó."

**Si repregunta — ¿dónde se valida?** "En `CommentRequest`, el contenido tiene una validación que no permite texto vacío, y el `@Valid` del controlador la activa."

---

# Los otros 5 casos

No están en el Excel, pero pueden preguntarlos porque cubren el login, los usuarios y el conflicto de versión.

<a id="cp-01"></a>
### CP-01 POST /auth/login con credenciales correctas devuelve 200 y un token

**Abrir:** `AuthControllerTest` → `login_withValidCredentials_returnsToken`

| | |
|---|---|
| Entrada | Usuario "admin" con su contraseña correcta |
| Salida esperada | 200 y un JSON con el campo "token" |
| Verificación | El token no está vacío y, al abrirlo, su dueño es "admin" |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void login_withValidCredentials_returnsToken() throws Exception {` | Nombre del método: login con credenciales válidas devuelve un token. |
| 2 | `MvcResult result = mockMvc.perform(post(API + "/auth/login")` | Envía un `POST` al login y guarda el resultado completo en `result` para leerlo después. No lleva token: es la única ruta pública. |
| 3 | `.contentType(MediaType.APPLICATION_JSON)` | Indica que el cuerpo de la petición es JSON (cabecera `Content-Type`). |
| 4 | `.content("""` | Abre el cuerpo de la petición. |
| 5 | `{"username": "admin", "password": "admin123"}` | JSON que se convierte en `LoginRequest`: usuario y contraseña correctos. |
| 6 | `"""))` | Cierra el cuerpo y el `perform`. |
| 7 | `.andExpect(status().isOk())` | Salida esperada: código 200. |
| 8 | `.andExpect(jsonPath("$.token").isNotEmpty())` | La respuesta debe traer el campo `token` con contenido. |
| 9 | `.andReturn();` | Devuelve el resultado de la petición para poder extraer el token. |
| 10 | `String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");` | Toma el cuerpo de la respuesta como texto y extrae el valor del campo `token`. |
| 11 | `assertThat(jwtService.extractUsername(token)).isEqualTo("admin");` | Verificación: `JwtService` valida la firma del token y lee su dueño, que debe ser "admin". |
| 12 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Este caso prueba el login. Mandamos un POST al login con el usuario admin y su contraseña. Aquí no va token, porque el login es la única ruta pública."

"Esperamos un 200 y que el campo `token` no esté vacío."

"Pero no nos quedamos ahí. Las dos últimas líneas sacan el token de la respuesta y lo abren con `JwtService`. Comprobamos que el dueño del token sea admin. Así verificamos que el token es válido y pertenece a quien inició sesión."

**Si repregunta — ¿de dónde sale el usuario admin?** "De la base de datos. Se crea antes de cada prueba con su contraseña cifrada con BCrypt."

<a id="cp-02"></a>
### CP-02 POST /auth/login con contraseña incorrecta devuelve 401

**Abrir:** `AuthControllerTest` → `login_withWrongPassword_returns401`

| | |
|---|---|
| Entrada | Usuario "admin" con una contraseña equivocada |
| Salida esperada | 401 con el motivo en "detail" |
| Verificación | El código es 401 y la respuesta no trae ningún token |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void login_withWrongPassword_returns401() throws Exception {` | Nombre del método: login con contraseña incorrecta da 401. |
| 2 | `mockMvc.perform(post(API + "/auth/login")` | Envía un `POST` al login, sin token. |
| 3 | `.contentType(MediaType.APPLICATION_JSON)` | Indica que el cuerpo de la petición es JSON (cabecera `Content-Type`). |
| 4 | `.content("""` | Abre el cuerpo de la petición. |
| 5 | `{"username": "admin", "password": "no-es-la-clave"}` | El usuario existe, pero la contraseña no coincide con el hash guardado. |
| 6 | `"""))` | Cierra el cuerpo y el `perform`. |
| 7 | `.andExpect(status().isUnauthorized())` | Salida esperada: código 401. `AuthService` lanza `BadCredentialsException`. |
| 8 | `.andExpect(jsonPath("$.detail").value("Usuario o contraseña incorrectos"))` | El mensaje lo arma `GlobalExceptionHandler`; no revela cuál de los dos datos falló. |
| 9 | `.andExpect(jsonPath("$.token").doesNotExist());` | Verificación: la respuesta no trae ningún token. |
| 10 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Aquí intentamos entrar como admin con una contraseña equivocada."

"Esperamos un 401. Revisamos que el mensaje diga 'Usuario o contraseña incorrectos' y, lo más importante, que en la respuesta no exista el campo `token`."

**Si repregunta — ¿por qué el mensaje no dice cuál de los dos está mal?** "Por seguridad. Si dijéramos 'la contraseña está mal', le confirmaríamos a un atacante que ese usuario sí existe."

<a id="cp-03"></a>
### CP-03 POST /users como ADMIN crea el usuario y devuelve 201

**Abrir:** `UserControllerTest` → `create_asAdmin_returns201`

| | |
|---|---|
| Entrada | Token de admin + los datos del usuario "luis" |
| Salida esperada | 201 y el usuario creado, sin contraseña |
| Verificación | La respuesta no trae contraseña y en la base quedó el hash BCrypt |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `private static final String LUIS = """ { "username": "luis", "password": "luis123", "fullName": "Luis Ramos", "email": "luis@tickets.pe", "role": "USER" } """;` | Constante de la clase con el JSON del usuario a crear. La comparten CP-03 y CP-04. |
| 2 | `void create_asAdmin_returns201() throws Exception {` | Nombre del método: crear un usuario como ADMIN da 201. |
| 3 | `mockMvc.perform(post(API + "/users")` | Envía un `POST` a `/api/v1/users`. |
| 4 | `.header("Authorization", bearer("admin"))` | Agrega la cabecera `Authorization` con un token JWT real de admin, generado por `JwtService`. |
| 5 | `.contentType(MediaType.APPLICATION_JSON)` | Indica que el cuerpo de la petición es JSON (cabecera `Content-Type`). |
| 6 | `.content(LUIS))` | El cuerpo es el JSON de la constante `LUIS`, que se convierte en `UserRequest`. |
| 7 | `.andExpect(status().isCreated())` | Salida esperada: código 201. |
| 8 | `.andExpect(jsonPath("$.id").isNumber())` | Atributo 1: `id` numérico. |
| 9 | `.andExpect(jsonPath("$.username").value("luis"))` | Atributo 2: `username`. |
| 10 | `.andExpect(jsonPath("$.fullName").value("Luis Ramos"))` | Atributo 3: `fullName`. |
| 11 | `.andExpect(jsonPath("$.email").value("luis@tickets.pe"))` | Atributo 4: `email`. |
| 12 | `.andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))` | Atributo 5: `roles`. Se envió "USER" y se guardó como `ROLE_USER`. |
| 13 | `.andExpect(jsonPath("$.password").doesNotExist())` | La respuesta no debe traer la contraseña. |
| 14 | `.andExpect(jsonPath("$.passwordHash").doesNotExist());` | Tampoco debe traer su hash. |
| 15 | `User saved = userRepository.findByUsername("luis").orElseThrow();` | Verificación en la base: busca al usuario recién creado. Falla si no existe. |
| 16 | `assertThat(saved.getPasswordHash()).isNotEqualTo("luis123");` | Lo guardado no es la contraseña en texto plano. |
| 17 | `assertThat(passwordEncoder.matches("luis123", saved.getPasswordHash())).isTrue();` | BCrypt confirma que ese hash corresponde a "luis123". |
| 18 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Este caso prueba la creación de usuarios. Mandamos un POST con el token de admin y los datos de Luis: usuario, contraseña, nombre completo, correo y rol."

"Esperamos un 201. En la respuesta revisamos sus cinco atributos: el id, el usuario, el nombre completo, el correo y el rol."

"Luego hay dos líneas con `doesNotExist`. Comprueban que la respuesta no trae ni la contraseña ni su hash."

"Y al final vamos a la base. Buscamos a Luis y comprobamos dos cosas: que lo guardado no es igual a 'luis123', o sea, que no está en texto plano; y que BCrypt reconoce que ese hash corresponde a 'luis123'."

**Si repregunta — ¿qué es BCrypt?** "Es un algoritmo que convierte la contraseña en un hash que no se puede revertir. Guardamos el hash, nunca la contraseña."

<a id="cp-04"></a>
### CP-04 POST /users como USER devuelve 403 y no crea nada

**Abrir:** `UserControllerTest` → `create_asRegularUser_returns403`

| | |
|---|---|
| Entrada | Token de Ana (ROLE_USER) + los mismos datos |
| Salida esperada | 403, porque solo un ADMIN puede crear usuarios |
| Verificación | El código es 403 y "luis" no existe en la base |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void create_asRegularUser_returns403() throws Exception {` | Nombre del método: crear un usuario como usuario normal da 403. |
| 2 | `mockMvc.perform(post(API + "/users")` | Envía un `POST` a `/api/v1/users`. |
| 3 | `.header("Authorization", bearer("ana"))` | Agrega la cabecera `Authorization` con un token JWT real de ana, generado por `JwtService`. |
| 4 | `.contentType(MediaType.APPLICATION_JSON)` | Indica que el cuerpo de la petición es JSON (cabecera `Content-Type`). |
| 5 | `.content(LUIS))` | El mismo JSON válido de CP-03: lo único que cambia es quién lo envía. |
| 6 | `.andExpect(status().isForbidden());` | Salida esperada: código 403. `@PreAuthorize("hasRole('ADMIN')")` rechaza a ana. |
| 7 | `assertThat(userRepository.existsByUsername("luis")).isFalse();` | Verificación en la base: el usuario "luis" no existe, no se creó nada. |
| 8 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Es el mismo POST para crear a Luis, pero con el token de Ana, que no es administradora."

"Esperamos un 403. Y después comprobamos en la base que el usuario Luis no existe. Igual que en los otros casos de permisos, verificamos que además de responder el error, no se hizo nada."

<a id="cp-21"></a>
### CP-21 PUT /tickets/{id}/status con una versión antigua devuelve 409

**Abrir:** `TicketControllerTest` → `changeStatus_withStaleVersion_returns409`

| | |
|---|---|
| Entrada | Token de admin + nuevo estado CLOSED + una versión que no es la actual (99) |
| Salida esperada | 409, el ticket cambió desde que el cliente lo leyó |
| Verificación | El código es 409 y en la base el ticket sigue OPEN |

**Código línea por línea:**

| # | Código | Qué hace |
|---|---|---|
| 1 | `void changeStatus_withStaleVersion_returns409() throws Exception {` | Nombre del método: cambiar el estado con una versión antigua da 409. |
| 2 | `mockMvc.perform(put(API + "/tickets/{id}/status", ticket.getId())` | Envía un `PUT` a `/api/v1/tickets/{id}/status` con el id del ticket de prueba. |
| 3 | `.header("Authorization", bearer("admin"))` | Agrega la cabecera `Authorization` con un token JWT real de admin, generado por `JwtService`. |
| 4 | `.contentType(MediaType.APPLICATION_JSON)` | Indica que el cuerpo de la petición es JSON (cabecera `Content-Type`). |
| 5 | `.content("""` | Abre el cuerpo de la petición. |
| 6 | `{"status": "CLOSED", "version": 99}` | El rol es correcto, pero la versión 99 no coincide con la actual (0). |
| 7 | `"""))` | Cierra el cuerpo y el `perform`. |
| 8 | `.andExpect(status().isConflict());` | Salida esperada: código 409. El servicio compara versiones y lanza `ConflictException`. |
| 9 | `assertThat(ticketRepository.findById(ticket.getId()).orElseThrow().getStatus())` | Verificación en la base: vuelve a leer el ticket y toma su estado. |
| 10 | `.isEqualTo(TicketStatus.OPEN);` | El ticket debe seguir `OPEN`: el cambio fue rechazado. |
| 11 | `}` | Fin del método. Si ninguna comprobación falló, la prueba pasa. |

**Leer:**

"Este caso prueba el control de cambios simultáneos. El administrador intenta cerrar el ticket, pero manda la versión 99, cuando la versión real es cero."

"Esperamos un 409, que significa 'conflicto'. La API entiende que el cliente tiene una copia vieja del ticket."

"Y comprobamos en la base que el ticket sigue en OPEN, o sea, que no se tocó."

**Si repregunta — ¿cuándo pasaría esto en la vida real?** "Cuando dos administradores abren el mismo ticket a la vez. El primero que guarda sube la versión. Cuando el segundo intenta guardar con la versión vieja, recibe el 409 y tiene que volver a cargar el ticket."

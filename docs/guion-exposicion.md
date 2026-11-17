# Guion de exposición — Gestor de Tickets (Equipo 16)

Lo que está entre comillas es para leerlo tal cual. Lo que dice **Mostrar** o **Ejecutar** es lo que se hace en pantalla mientras se habla.

Orden de la exposición:

1. Presentación
2. Ejecutar las pruebas
3. Paso previo: login y usuario
4. Las 8 operaciones, en orden, mostrando la rúbrica sobre la marcha
5. Cierre
6. Preguntas de práctica con sus respuestas

---

## Antes de empezar (sin público)

- Arrancar la API con `./gradlew bootRun` en una terminal. No arrancarla desde el IDE.
- Tener abiertos: una segunda terminal (o Postman), el IDE con el proyecto y este guion.
- Hacer un login de prueba para confirmar que la base de datos responde.
- En la terminal de la demo, dejar listas estas dos variables:

```bash
API=http://localhost:8080/api/v1
JSON="Content-Type: application/json"
```

---

## 1. Presentación (30 segundos)

"Buenas, somos el equipo 16. Nuestro proyecto es una API REST para gestionar tickets de soporte. La idea es simple: una persona reporta un problema, lo clasifica en una categoría, se le pueden agregar comentarios y un administrador va cambiando su estado hasta cerrarlo."

"Está hecho con Spring Boot y Java, la base de datos es PostgreSQL y la seguridad funciona con tokens JWT."

"Vamos a hacer tres cosas: primero ejecutamos las pruebas, después recorremos las ocho operaciones una por una, y mientras las mostramos vamos señalando cada punto de la rúbrica."

---

## 2. Ejecutar las pruebas (2 minutos)

**Ejecutar:**

```bash
./gradlew cleanTest test
```

**Decir mientras corre:**

"Empezamos por las pruebas porque son la evidencia de que todo lo que vamos a mostrar funciona. Tenemos 31 pruebas en total."

"De esas, 21 son de integración. Una prueba de integración levanta la aplicación completa y le manda una petición HTTP de verdad, con su token, así que pasa por la seguridad, el controlador, el servicio y la base de datos."

"Otras 9 son unitarias: prueban solo la lógica de un servicio, sin base de datos. Y hay una más que solo verifica que la aplicación arranca."

"Las pruebas no tocan nuestra base real. Usan una base H2 que vive en memoria: nace vacía, se llena con datos conocidos antes de cada prueba y desaparece al terminar."

**Mostrar:** la lista de pruebas en verde y el `BUILD SUCCESSFUL`.

"Cada operación tiene como mínimo dos casos de prueba: uno donde todo sale bien y otro donde algo falla. Y cada caso tiene tres partes: la entrada, la salida esperada y la verificación."

**Mostrar en el IDE:** `TicketControllerTest`, la prueba CP-13, señalando el comentario de arriba.

"Aquí se ve. La entrada es el token de Ana más los datos del ticket. La salida esperada es un 201 con el ticket creado. Y la verificación no se queda en el código de respuesta: también revisa que en la base ahora haya dos tickets."

---

## 3. Paso previo: login y usuario (1 minuto y medio)

"Antes de usar cualquier operación necesitamos un token, porque toda la API está protegida."

**Ejecutar:**

```bash
curl -s -X POST $API/auth/login -H "$JSON" -d '{"username":"admin","password":"admin123"}'
```

"Mandamos usuario y contraseña, y la API nos devuelve un token JWT. Ese token es como un pase firmado: dice quién soy y hasta cuándo vale. De aquí en adelante lo enviamos en cada petición, en la cabecera Authorization."

**Ejecutar** (guardar los dos tokens):

```bash
TOKEN_ADMIN=$(curl -s -X POST $API/auth/login -H "$JSON" -d '{"username":"admin","password":"admin123"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')
TOKEN_ANA=$(curl -s -X POST $API/auth/login -H "$JSON" -d '{"username":"ana","password":"ana123"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')
```

"Tenemos dos usuarios: admin, que tiene el rol de administrador, y Ana, que es una usuaria normal. Los dos están guardados en la base de datos, no en el código."

**Ejecutar** (crear un usuario):

```bash
curl -s -X POST $API/users -H "Authorization: Bearer $TOKEN_ADMIN" -H "$JSON" \
  -d '{"username":"luis","password":"luis123","fullName":"Luis Ramos","email":"luis@tickets.pe","role":"USER"}'
```

"Y los usuarios también se crean por la API. Fíjense en la respuesta: trae el id, el usuario, el nombre completo, el correo y los roles. Son cinco atributos. Lo que no trae es la contraseña, porque en la base solo guardamos su versión cifrada con BCrypt."

> **Rúbrica que se cubre aquí:** tokens JWT, usuarios desde base de datos, y el objeto **Usuario** (5 atributos), que es el cuarto objeto completo.

Nota: si `luis` ya existe de un ensayo anterior, la API responde 409. Cambiar el nombre y el correo antes de exponer.

---

## 4. Las 8 operaciones

"Ahora sí, las ocho operaciones. Están repartidas en tres controladores: categorías, tickets y comentarios. Todas leen o escriben en la base de datos."

### Operación 1 — `GET /categories` (listar categorías)

**Ejecutar caso 1:**

```bash
curl -s -w '\n[%{http_code}]\n' $API/categories -H "Authorization: Bearer $TOKEN_ANA"
```

"Con el token de Ana pedimos la lista de categorías. Responde 200 y cada categoría viene con sus cuatro atributos: id, nombre, descripción y si está activa."

**Ejecutar caso 2:**

```bash
curl -s -w '\n[%{http_code}]\n' $API/categories
```

"Y si hago lo mismo sin token, responde 401. Esto pasa con todas las rutas menos el login."

> **Rúbrica:** objeto **Categoría** (4 atributos) · seguridad: sin token no se entra.

### Operación 2 — `POST /categories` (crear categoría)

**Ejecutar caso 1:**

```bash
curl -s -w '\n[%{http_code}]\n' -X POST $API/categories -H "Authorization: Bearer $TOKEN_ADMIN" -H "$JSON" \
  -d '{"name":"Accesos","description":"Cuentas, contraseñas y permisos"}'
```

"Crear una categoría solo lo puede hacer un administrador. Con el token de admin responde 201, que significa creado, y nos devuelve la categoría ya con su id."

**Ejecutar caso 2:**

```bash
curl -s -w '\n[%{http_code}]\n' -X POST $API/categories -H "Authorization: Bearer $TOKEN_ADMIN" -H "$JSON" \
  -d '{"name":"","description":"Sin nombre"}'
```

"Si mando el nombre vacío, responde 400 y dentro de 'errors' me dice exactamente qué campo falló. No se guarda nada."

> **Rúbrica:** mecanismo de seguridad por roles · validación de datos.

Nota: si "Accesos" ya existe de un ensayo, cambiar el nombre.

### Operación 3 — `GET /tickets` (listar tickets)

**Ejecutar caso 1:**

```bash
curl -s -w '\n[%{http_code}]\n' $API/tickets -H "Authorization: Bearer $TOKEN_ANA"
```

"Esta es la lista de tickets. No devuelve todos de golpe: viene paginada. En la respuesta está el contenido, el número de página, el tamaño y el total. La primera página es la cero, que es la convención de Spring."

**Ejecutar caso 2:**

```bash
curl -s -w '\n[%{http_code}]\n' "$API/tickets?status=CLOSED" -H "Authorization: Bearer $TOKEN_ANA"
```

"También se puede filtrar por estado. Aquí pido solo los cerrados, y la respuesta sigue siendo 200 pero solo con los que cumplen el filtro."

> **Rúbrica:** objeto **Ticket** · acceso a base de datos con paginación y filtro.

### Operación 4 — `GET /tickets/{id}` (ver un ticket)

**Ejecutar caso 1:**

```bash
curl -s -w '\n[%{http_code}]\n' $API/tickets/1 -H "Authorization: Bearer $TOKEN_ANA"
```

"Este es el detalle de un ticket, y es nuestro objeto más completo. Tiene diez atributos: id, título, descripción, estado, prioridad, categoría, creador, versión, fecha de creación y fecha de actualización."

**Ejecutar caso 2:**

```bash
curl -s -w '\n[%{http_code}]\n' $API/tickets/999 -H "Authorization: Bearer $TOKEN_ANA"
```

"Si pido uno que no existe, responde 404 y en el mensaje dice 'No existe el ticket 999'."

> **Rúbrica:** objeto **Ticket** completo (10 atributos).

### Operación 5 — `POST /tickets` (crear ticket)

**Ejecutar caso 1:**

```bash
curl -s -w '\n[%{http_code}]\n' -X POST $API/tickets -H "Authorization: Bearer $TOKEN_ANA" -H "$JSON" \
  -d '{"title":"Sin internet en el aula B204","description":"Ninguna PC del aula tiene red","priority":"MEDIUM","categoryId":1}'
```

"Ana crea un ticket. Responde 201. Miren tres cosas en la respuesta: el estado es OPEN, la versión es cero y el creador es Ana."

"Lo importante es que yo no mandé ni el estado ni el creador. El estado lo decide el sistema: todo ticket nace abierto. Y el creador sale del token, no del JSON. Así nadie puede crear un ticket a nombre de otra persona."

**Ejecutar caso 2:**

```bash
curl -s -w '\n[%{http_code}]\n' -X POST $API/tickets -H "Authorization: Bearer $TOKEN_ANA" -H "$JSON" \
  -d '{"title":"","description":"Ninguna PC del aula tiene red","priority":"MEDIUM","categoryId":1}'
```

"Sin título responde 400, y en 'errors' aparece el campo 'title'."

> **Rúbrica:** el usuario se toma del token y se busca en la base de datos.

### Operación 6 — `PUT /tickets/{id}/status` (cambiar estado)

**Antes:** mirar qué `version` tiene el ticket en la respuesta de la operación 4 y usar ese número.

**Ejecutar caso 1:**

```bash
curl -s -w '\n[%{http_code}]\n' -X PUT $API/tickets/1/status -H "Authorization: Bearer $TOKEN_ADMIN" -H "$JSON" \
  -d '{"status":"IN_PROGRESS","version":0}'
```

"El administrador pasa el ticket a 'en progreso'. Responde 200, el estado cambió y la versión subió en uno."

"¿Para qué mando la versión? Es para que dos personas no se pisen. Yo le digo a la API qué versión leí. Si alguien más cambió el ticket mientras tanto, la versión ya no coincide y la API rechaza mi cambio."

**Ejecutar caso 2:**

```bash
curl -s -w '\n[%{http_code}]\n' -X PUT $API/tickets/1/status -H "Authorization: Bearer $TOKEN_ANA" -H "$JSON" \
  -d '{"status":"CLOSED","version":1}'
```

"Si lo intenta Ana, responde 403. Ojo con la diferencia: 401 es 'no sé quién eres' y 403 es 'sé quién eres, pero no tienes permiso'."

**Si hay tiempo**, repetir el comando del caso 1 tal cual:

"Y si repito el primer comando con la versión vieja, responde 409, conflicto. El ticket no se toca."

> **Rúbrica:** mecanismo de seguridad por roles · control de cambios simultáneos.

### Operación 7 — `GET /tickets/{id}/comments` (listar comentarios)

**Ejecutar caso 1:**

```bash
curl -s -w '\n[%{http_code}]\n' $API/tickets/1/comments -H "Authorization: Bearer $TOKEN_ANA"
```

"Estos son los comentarios de un ticket. Cada uno trae cinco atributos: id, contenido, autor, id del ticket y fecha."

**Ejecutar caso 2:**

```bash
curl -s -w '\n[%{http_code}]\n' $API/tickets/999/comments -H "Authorization: Bearer $TOKEN_ANA"
```

"Si el ticket no existe, responde 404."

> **Rúbrica:** objeto **Comentario** (5 atributos).

Nota: si el ticket 1 no tiene comentarios, la lista sale vacía. Conviene dejarle uno creado en el ensayo.

### Operación 8 — `POST /tickets/{id}/comments` (comentar)

**Ejecutar caso 1:**

```bash
curl -s -w '\n[%{http_code}]\n' -X POST $API/tickets/1/comments -H "Authorization: Bearer $TOKEN_ADMIN" -H "$JSON" \
  -d '{"content":"Revisaremos la fuente de poder"}'
```

"El administrador comenta el ticket. Responde 201 y el autor es admin. Igual que antes, el autor sale del token."

**Ejecutar caso 2:**

```bash
curl -s -w '\n[%{http_code}]\n' -X POST $API/tickets/1/comments -H "Authorization: Bearer $TOKEN_ADMIN" -H "$JSON" \
  -d '{"content":""}'
```

"Con el texto vacío responde 400 y no se guarda nada."

> **Rúbrica:** con esta se completan las 8 operaciones.

---

## 5. Cierre (30 segundos)

"Para cerrar, repasamos la rúbrica."

"En operaciones REST: mostramos ocho operaciones con acceso a base de datos en tres controladores, y cuatro objetos completos: Ticket con diez atributos, Comentario con cinco, Usuario con cinco y Categoría con cuatro."

"En seguridad: usamos tokens JWT, los usuarios y sus roles están en la base de datos, las contraseñas se guardan cifradas con BCrypt y hay operaciones que solo puede hacer un administrador."

"Y en pruebas: cada operación tiene al menos dos casos, con entrada, salida esperada y verificación, y las 31 pruebas pasan."

"Eso es todo. Quedamos atentos a sus preguntas."

---

# Preguntas de práctica

Las respuestas están escritas para decirlas en voz alta. Abran el archivo que se menciona mientras responden.

## A. Seguridad

### ¿Cuáles son los archivos de seguridad y qué hace cada uno?

"Son cinco y están en la carpeta `security`."

"El primero es **JwtProperties**. Solo guarda dos datos de configuración: la clave secreta con la que firmamos los tokens y cuánto tiempo duran. La clave secreta viene del archivo `.env`, no está escrita en el código."

"El segundo es **JwtService**. Hace dos cosas: crear un token y leer un token. Para crearlo le pone el nombre de usuario, la fecha de emisión, la fecha de vencimiento y lo firma con la clave secreta. Para leerlo verifica la firma y devuelve el nombre de usuario. Si el token es falso, está vencido o está mal formado, lanza un error."

"El tercero es **CustomUserDetailsService**. Es el que va a la base de datos. Recibe un nombre de usuario, lo busca en la tabla de usuarios, saca sus roles y se lo entrega a Spring Security en el formato que Spring entiende."

"El cuarto es **JwtAuthenticationFilter**. Es un filtro, o sea, un código que se ejecuta antes de que la petición llegue al controlador. Lee la cabecera Authorization, saca el token, le pide a JwtService el nombre de usuario, le pide a CustomUserDetailsService los datos de ese usuario y lo deja anotado como 'usuario autenticado' para el resto de la petición."

"Y el quinto es **SecurityConfig**. Ahí están las reglas: qué rutas son públicas, cuáles piden token, que no usamos sesiones y que nuestro filtro se ejecuta antes que los de Spring. También define que las contraseñas se cifran con BCrypt."

### ¿Cómo pasa una petición por esos archivos?

"Hay dos momentos distintos: el login y el resto de peticiones."

"En el **login**, la petición llega a `AuthController`, que llama a `AuthService`. Ese servicio le pide a Spring que verifique el usuario y la contraseña. Spring usa `CustomUserDetailsService` para traer al usuario de la base y compara la contraseña que mandé con la que está cifrada con BCrypt. Si coinciden, `JwtService` genera el token y se lo devolvemos al cliente. Si no coinciden, respondemos 401."

"En **cualquier otra petición**, por ejemplo listar tickets, el camino es este. Primero la atrapa `JwtAuthenticationFilter`. Lee la cabecera, que viene como 'Bearer' y el token. Le quita la palabra Bearer y se queda con el token. Luego `JwtService` verifica la firma y saca el nombre de usuario. Después `CustomUserDetailsService` busca a ese usuario en la base con sus roles. Y el filtro lo guarda en el contexto de seguridad."

"Después de eso, Spring revisa las reglas de `SecurityConfig`. Si la ruta pide estar autenticado y no hay nadie en el contexto, responde 401. Si hay alguien, la petición sigue hasta el controlador. Y si el método tiene un `@PreAuthorize` que pide ser administrador y el usuario no lo es, responde 403."

### ¿Qué pasa si el token es falso o está vencido?

"El filtro intenta leerlo y `JwtService` lanza un error. El filtro atrapa ese error y no hace nada: deja que la petición siga, pero como anónima. Como nadie quedó autenticado, las reglas de seguridad responden 401. Lo hicimos así para que haya un solo lugar que decide quién pasa y quién no."

### ¿Qué es un JWT y por qué lo usan en vez de sesiones?

"Un JWT es un texto con tres partes separadas por puntos: una cabecera, los datos y una firma. En los datos va el nombre de usuario y la fecha de vencimiento. La firma se calcula con una clave que solo conoce el servidor, así que si alguien modifica el token, la firma ya no coincide y lo rechazamos."

"Lo usamos en vez de sesiones porque así el servidor no tiene que recordar a nadie. Cada petición trae todo lo necesario para saber quién es. Eso se llama ser 'stateless', sin estado, y en `SecurityConfig` está configurado con `SessionCreationPolicy.STATELESS`."

### ¿Por qué desactivaron CSRF?

"CSRF es un ataque que se aprovecha de las cookies de sesión que el navegador envía solo. Nosotros no usamos cookies ni sesiones: el token viaja en una cabecera que el cliente tiene que poner a propósito. Por eso esa protección no nos aplica."

### ¿Dónde se ve que los usuarios vienen de la base de datos?

"En `CustomUserDetailsService`, en la línea que dice `userRepository.findByUsername`. Esa consulta se ejecuta en el login y también en cada petición con token. Las tablas son `users`, `roles` y `user_roles`, y se crean con las migraciones de Flyway."

### ¿Cómo guardan las contraseñas?

"Nunca en texto plano. Antes de guardar un usuario, la contraseña pasa por BCrypt, que la convierte en un hash. Un hash no se puede revertir: no hay forma de obtener la contraseña original a partir de él. En el login, BCrypt cifra lo que escribió el usuario y compara los dos hashes."

### ¿Cuál es la diferencia entre 401 y 403?

"401 es que no sabemos quién eres: no mandaste token o el token no sirve. 403 es que sí sabemos quién eres, pero tu rol no alcanza para esa operación. Por ejemplo, Ana intentando cambiar el estado de un ticket."

## B. El flujo de crear un ticket

### Explique paso a paso qué pasa cuando se crea un ticket

"Lo cuento en orden, desde que sale la petición hasta que vuelve la respuesta."

"**Uno.** El cliente manda un POST a `/api/v1/tickets` con el token en la cabecera y un JSON con título, descripción, prioridad y el id de la categoría."

"**Dos.** La petición llega primero a `JwtAuthenticationFilter`. Valida el token, busca a Ana en la base y la deja como usuaria autenticada."

"**Tres.** `SecurityConfig` revisa que la ruta pide estar autenticado. Como Ana lo está, pasa."

"**Cuatro.** Llega a `TicketController`, al método `create`. Spring convierte el JSON en un objeto `TicketRequest` y lo valida. Si el título viene vacío, aquí se corta y responde 400."

"**Cinco.** El controlador llama a `ticketService.create` y le pasa dos cosas: los datos y el nombre de usuario que sacó del token."

"**Seis.** En `TicketService`, primero busca la categoría por su id. Si no existe, lanza un error y se responde 400. Después busca al usuario por su nombre. Luego construye el ticket, que nace con estado OPEN, y lo guarda con `ticketRepository.save`."

"**Siete.** El repositorio le pasa el ticket a Hibernate, que arma el INSERT y lo ejecuta en PostgreSQL. La base le asigna el id."

"**Ocho.** El servicio convierte el ticket guardado en un `TicketResponse`, que es lo que le mostramos al cliente."

"**Nueve.** El controlador lo devuelve, Spring lo convierte a JSON y responde con el código 201."

### ¿Qué significa cada anotación del controlador?

**Mostrar:** `TicketController`.

"**@RestController** le dice a Spring que esta clase atiende peticiones HTTP y que lo que devuelven sus métodos se convierte directo a JSON."

"**@RequestMapping** con la ruta de tickets define el prefijo. Todos los métodos de esta clase cuelgan de `/api/v1/tickets`."

"**@GetMapping, @PostMapping y @PutMapping** indican qué verbo HTTP atiende cada método. Si llevan algo entre paréntesis, como `/{id}`, eso se agrega a la ruta."

"**@PathVariable** toma un valor que viene en la ruta. En `/tickets/5`, ese cinco llega al método como el parámetro `id`."

"**@RequestParam** toma un valor que viene después del signo de interrogación, como `status=OPEN`. Aquí dice `required = false` porque el filtro es opcional."

"**@RequestBody** le dice a Spring que convierta el JSON del cuerpo de la petición en un objeto Java."

"**@Valid** hace que ese objeto se valide antes de entrar al método. Si no cumple las reglas, responde 400 sin ejecutar nada."

"**@ResponseStatus(HttpStatus.CREATED)** hace que el método responda 201 en vez del 200 normal, porque se creó algo."

"**@PreAuthorize("hasRole('ADMIN')")** revisa el rol antes de ejecutar el método. Si el usuario no es administrador, responde 403."

"**@PageableDefault** pone los valores por defecto de la paginación: veinte por página y los más nuevos primero."

"Y el parámetro **Authentication** no es una anotación, pero es importante: Spring nos entrega ahí al usuario que el filtro dejó autenticado. De ahí sacamos el nombre con `getName`."

### ¿Y las anotaciones del `TicketRequest`?

"`TicketRequest` es un record, que es una clase corta de Java que solo guarda datos y no se puede modificar."

"**@NotBlank** exige que el texto no venga nulo ni vacío ni solo con espacios. **@Size** pone un máximo de caracteres: 150 para el título y 2000 para la descripción. Y **@NotNull** exige que el valor venga, y lo usamos para la prioridad y la categoría."

### ¿Y las anotaciones del servicio?

**Mostrar:** `TicketService`.

"**@Service** registra la clase en Spring como un componente de lógica de negocio, para que Spring la cree y se la entregue al controlador."

"**@Transactional(readOnly = true)**, arriba de la clase, hace que por defecto todos los métodos trabajen en una transacción de solo lectura, que es más liviana."

"**@Transactional** a secas, sobre `create` y `changeStatus`, cambia eso para los métodos que escriben. Una transacción significa que todo se hace o nada se hace: si algo falla a la mitad, la base queda como estaba."

### ¿Y las anotaciones de la entidad `Ticket`?

**Mostrar:** `Ticket`.

"**@Entity** dice que esta clase representa una tabla. **@Table** dice cuál: `tickets`."

"**@Id** marca la llave primaria y **@GeneratedValue** dice que el número lo genera la base de datos."

"**@Column** ajusta la columna: si puede ser nula y su tamaño máximo."

"**@Enumerated(EnumType.STRING)** guarda el estado y la prioridad como texto, por ejemplo 'OPEN', y no como un número. Así la tabla se entiende al leerla."

"**@ManyToOne** es la relación: muchos tickets pertenecen a una categoría y muchos tickets son de un usuario. **@JoinColumn** dice qué columna es la llave foránea."

"Y **@Version** es el bloqueo optimista. Hibernate suma uno a ese número en cada actualización, y si alguien intenta guardar con un número viejo, rechaza el cambio."

### ¿Por qué usan `TicketRequest` y `TicketResponse` en vez de la entidad directamente?

"Por dos razones. La primera es control: el cliente solo puede mandar los cuatro campos del request. No puede mandar el estado, ni el creador, ni el id. La segunda es seguridad: en la respuesta decidimos qué mostramos. Por ejemplo, del usuario solo mostramos el nombre, nunca su contraseña."

### ¿Por qué el controlador no usa el repositorio directamente?

"Porque separamos responsabilidades en capas. El controlador solo sabe de HTTP: recibe la petición y devuelve la respuesta. El servicio tiene las reglas del negocio. Y el repositorio habla con la base de datos. Si mañana cambia una regla, la cambiamos en un solo lugar, y además podemos probar el servicio solo, sin levantar el servidor."

### ¿Qué pasa cuando algo falla? ¿Quién arma el error?

"El servicio lanza una excepción, por ejemplo `ResourceNotFoundException`. Esa excepción la atrapa `GlobalExceptionHandler`, que es una clase que escucha los errores de toda la aplicación y los convierte en una respuesta JSON con el código correcto: 404 si no existe, 400 si los datos son inválidos, 409 si hay conflicto. Así todos los errores tienen el mismo formato."

### ¿Cómo funciona el 409 al cambiar el estado?

"Cuando leo un ticket, viene con un número de versión. Para cambiarle el estado tengo que mandar ese mismo número. El servicio compara la versión que mandé con la actual. Si son distintas, significa que alguien lo cambió después de que yo lo leí, y respondemos 409 sin tocar nada. Si coinciden, se guarda y la versión sube en uno."

### ¿Por qué la página empieza en 0?

"Es la convención de Spring Data. Internamente calcula cuántos registros saltarse multiplicando la página por el tamaño. Con la página cero se salta cero registros, que es justo el inicio."

## C. Las pruebas de Tickets

### ¿Qué tipos de prueba tienen y en qué se diferencian?

"Tenemos dos tipos. Las **unitarias** prueban una sola clase aislada, en este caso el servicio, sin base de datos ni servidor. Son rapidísimas. Las de **integración** levantan toda la aplicación y mandan una petición HTTP completa, así que prueban todas las capas juntas, incluida la seguridad."

### ¿Qué es un mock?

"Un mock es un objeto de mentira que reemplaza a uno real. Tiene los mismos métodos, pero no hace nada por sí solo: responde lo que yo le diga que responda."

"Lo usamos para probar el servicio sin base de datos. El servicio necesita un repositorio, entonces le damos un repositorio falso y le decimos: 'cuando te pidan la categoría uno, devuelve Hardware'. Así probamos solo la lógica del servicio, y si la prueba falla sabemos que el problema está ahí y no en la base."

### Explique línea por línea la prueba unitaria de crear ticket

**Mostrar:** `TicketServiceTest`, prueba UT-05.

"Arriba de la clase está **@ExtendWith(MockitoExtension.class)**. Eso activa Mockito, que es la librería que crea los mocks."

"Luego está la constante **REQUEST**, que son los datos de entrada: un ticket llamado 'Sin internet', con prioridad media y categoría uno."

"Después vienen tres campos con **@Mock**: los repositorios de tickets, de categorías y de usuarios. Los tres son falsos."

"Y un campo con **@InjectMocks**, que es el `TicketService`. Ese sí es real, es la clase que estamos probando. Mockito lo crea y le mete adentro los tres repositorios falsos."

"Más abajo hay dos objetos de apoyo: una categoría Hardware y la usuaria Ana."

"Ahora la prueba. **@Test** marca el método como una prueba y **@DisplayName** le pone el nombre que se ve en el reporte."

"Las tres primeras líneas preparan los mocks. La primera dice: cuando el servicio busque la categoría uno, devuelve Hardware. La segunda: cuando busque al usuario Ana, devuelve a Ana. Y la tercera: cuando guarde cualquier ticket, devuelve el mismo ticket que recibió. Eso último imita lo que haría la base."

"Después viene la línea que ejecuta lo que queremos probar: `ticketService.create` con los datos y el nombre 'ana'."

"Y al final están las verificaciones con **assertThat**. Comprobamos que el estado sea OPEN, que el creador sea Ana y que la categoría sea Hardware."

"Con eso probamos una regla de negocio: todo ticket nace abierto y a nombre de quien lo creó."

### ¿Qué hacen `when`, `thenReturn`, `verify` y `never`?

"`when` y `thenReturn` van juntos y sirven para programar un mock: 'cuando te llamen así, responde esto'."

"`thenAnswer` es parecido, pero en vez de un valor fijo calcula la respuesta. Lo usamos para devolver el mismo ticket que se intentó guardar."

"`verify` comprueba que un método del mock sí fue llamado. Y `verify` con `never` comprueba lo contrario: que nunca se llamó. Por ejemplo, cuando la categoría no existe verificamos que nunca se llamó a `save`, o sea, que no se guardó nada."

"`any(Ticket.class)` significa 'cualquier ticket', para no tener que indicar uno exacto."

### ¿Cómo prueban que se lanza un error?

**Mostrar:** prueba UT-06.

"Con `assertThatThrownBy`. Le pasamos el código que debería fallar y después decimos qué error esperamos. En este caso programamos el mock para que diga que la categoría no existe, llamamos a crear, y comprobamos que se lanza `InvalidRequestException` con el mensaje 'No existe la categoría 1'."

### En la prueba de versión, ¿por qué usan `ReflectionTestUtils`?

"Porque el número de versión normalmente lo pone Hibernate al guardar en la base, y la clase no tiene un método para cambiarlo. Como en la prueba unitaria no hay base de datos, lo ponemos a mano con esa utilidad, que permite escribir en un campo privado. Solo se usa en pruebas."

### Explique línea por línea una prueba de integración de Tickets

**Mostrar:** `BaseIntegrationTest` y luego CP-13 en `TicketControllerTest`.

"Todas las pruebas de integración heredan de `BaseIntegrationTest`, que tiene tres anotaciones. **@SpringBootTest** levanta la aplicación completa. **@AutoConfigureMockMvc** nos da MockMvc, que es una herramienta para mandar peticiones HTTP sin abrir un puerto de verdad. Y **@ActiveProfiles("test")** activa la configuración de pruebas, que usa la base H2 en memoria."

"**@Autowired** le pide a Spring que nos entregue un objeto ya creado, por ejemplo los repositorios."

"El método con **@BeforeEach** se ejecuta antes de cada prueba. Borra todas las tablas y vuelve a crear los mismos datos: los roles, el usuario admin, la usuaria Ana, la categoría Hardware y un ticket de Ana. Así cada prueba empieza igual y ninguna depende de otra."

"Ahora la prueba CP-13. Empieza con `mockMvc.perform`, que manda la petición. Dentro va `post` con la ruta de tickets."

"`.header` agrega la cabecera Authorization. El método `bearer("ana")` genera un token real para Ana."

"`.contentType` avisa que el cuerpo es JSON y `.content` es el JSON, con título, descripción, prioridad y el id de la categoría."

"Después vienen los `andExpect`, que son la salida esperada. El primero pide que el código sea 201. Los demás usan `jsonPath` para mirar dentro del JSON de la respuesta: que el id sea un número, que el estado sea OPEN, que la prioridad sea MEDIUM, que la categoría sea Hardware, que el creador sea Ana y que la versión sea cero."

"Y la última línea es la verificación en la base: cuenta los tickets y espera que sean dos, el que ya había más el nuevo."

### ¿Por qué usan H2 en las pruebas y no la base real?

"Por tres motivos. Uno, para no ensuciar ni borrar los datos reales. Dos, porque es más rápida al estar en memoria. Y tres, para que las pruebas funcionen en cualquier computadora sin necesitar internet ni credenciales."

### ¿MockMvc es un mock como los de Mockito?

"No, y es fácil confundirlos por el nombre. MockMvc simula solo la parte de la red: no abre un puerto. Pero todo lo demás es real: el filtro de seguridad, el controlador, el servicio y la base H2. En cambio, los mocks de Mockito reemplazan objetos completos por versiones falsas."

### ¿Por qué prueban que Ana recibe 403 y además revisan la base?

"Porque el código de respuesta solo no alcanza. Podría responder 403 y aun así haber cambiado el ticket por un error. Por eso después de recibir el 403 volvemos a leer el ticket de la base y comprobamos que sigue en OPEN."

# UQDelivery — Contexto del proyecto

Este archivo es la fuente de verdad del proyecto para cualquier persona o agente de IA que trabaje en el código.
Contiene las decisiones de arquitectura, de dominio y de equipo. Si algo del código contradice este archivo,
gana este archivo (o se actualiza este archivo con la nueva decisión, en el mismo PR).

Idioma del proyecto: **español** (código de dominio, mensajes, documentación, commits).

---

## 1. Qué es el proyecto

Aplicación web de gestión de envíos y entregas de paquetes en entorno urbano. Proyecto universitario
(Universidad del Quindío, Ingeniería de Sistemas) desarrollado con Scrum por un equipo de 6 personas.

- **Roles (RBAC):** `ADMIN`, `EMISOR` (cliente que solicita envíos; corporativo y particular se tratan igual),
  `REPARTIDOR`. El **receptor final no tiene cuenta**: consulta su paquete con el código de seguimiento.
- **Alcance actual: Sprint 1** (2 semanas desde 2026-10-20): HU-00 a HU-05, con código, documentación y
  planeación completas. La semana 3 es opcional para historias del Sprint 2 (solo código, sin documentación
  de sprint).
- **Fuera de alcance:** pagos, integración con API de mapas (no la necesitan las HU del Sprint 1),
  desactivar cuentas y editar datos del perfil (quedan como posibles mejoras de HU-02).

### Requisitos no funcionales del enunciado

- Separación estricta entre front (SPA) y back (API REST).
- Base de datos relacional justificada con un diagrama del modelo (`docs/modelo-datos/diagrama-er.md`).
- Git con estrategia de ramas clara (ver sección 6).
- Cobertura de pruebas unitarias del back entre 60 y 70 %.
- Validación en cliente y servidor; protección contra inyección SQL y XSS.
- Backlog en herramienta de gestión (Trello) y aplicación desplegada en la nube.

### Historias del Sprint 1

| HU | Nombre | Rol | SP |
|---|---|---|---|
| HU-00 | Configuración inicial (estructura, entidades, seguridad, errores, despliegue base) | Equipo | — |
| HU-01 | Registro e inicio de sesión seguro | Usuario | 3 |
| HU-02 | Gestión de perfiles y roles (listar usuarios y cambiar rol) | Admin | 3 |
| HU-03 | Recuperación de contraseña por correo | Usuario | 2 |
| HU-04 | Creación de solicitud de envío | Emisor | 5 |
| HU-05 | Cotización automática de envíos | Emisor | 3 |

Total Sprint 1: **16 SP**. Los criterios de validación (Dado/Cuando/Entonces) de cada historia están en su
tarjeta de Trello. Qué archivos crea y toca cada historia está en `docs/guia-historias.md`.

### Product backlog completo

| HU | Épica | Nombre | SP | Prioridad (Wiegers) | Sprint |
|---|---|---|---|---|---|
| HU-01 | 1 · Usuarios y autenticación | Registro e inicio de sesión seguro | 3 | 1.74 | 1 |
| HU-02 | 1 · Usuarios y autenticación | Gestión de perfiles y roles | 3 | 1.55 | 1 |
| HU-03 | 1 · Usuarios y autenticación | Recuperación de contraseña | 2 | 1.58 | 1 |
| HU-04 | 2 · Envíos y ciclo de vida | Creación de solicitud de envío | 5 | 1.04 | 1 |
| HU-05 | 2 · Envíos y ciclo de vida | Cotización automática de envíos | 3 | 1.26 | 1 |
| HU-06 | 2 · Envíos y ciclo de vida | Asignación y reasignación de repartidores | 5 | 0.86 | 2 |
| HU-10 | 4 · Novedades y evidencia | Reporte de novedades en ruta | 3 | 1.26 | 2 |
| HU-11 | 4 · Novedades y evidencia | Cierre de entrega con evidencia digital | 5 | 0.93 | 2 |
| HU-12 | 5 · Notificaciones | Notificaciones por cambio de estado | 3 | 1.16 | 2 |
| HU-13 | 5 · Notificaciones | Alerta de cancelación de envío | 2 | 1.28 | 2 |
| HU-07 | 3 · Geolocalización | Visualización de ruta óptima del repartidor | 8 | 0.58 | 3 |
| HU-08 | 3 · Geolocalización | Rastreo en vivo del paquete | 8 | 0.65 | 3 |
| HU-09 | 3 · Geolocalización | Panel de control geográfico | 8 | 0.40 | 3 |
| HU-14 | 6 · Reportes y analítica | Historial de envíos personalizado | 3 | 1.06 | 3 |
| HU-15 | 6 · Reportes y analítica | Dashboard de rendimiento y métricas | 5 | 0.74 | 3 |
| HU-16 | 2 · Envíos y ciclo de vida | Configuración de tarifas y políticas (Admin) | — | — | Sin asignar |

Totales: Sprint 1 = 16 SP · Sprint 2 = 18 SP · Sprint 3 = 32 SP.

---

## 2. Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 21, Spring Boot 4.x, **Gradle (Groovy)** |
| Base de datos | PostgreSQL 17 (local con Docker Compose) |
| Frontend | Angular 22 (componentes standalone, signals, `@if`/`@for`), **Angular Material**, CSS |
| Seguridad | Spring Security + JWT (jjwt), BCrypt |
| Documentación API | springdoc-openapi (Swagger UI en `/swagger-ui.html`) |
| Correo | Brevo SMTP, puerto **2525** (Render gratis bloquea 25/465/587) |
| Despliegue | Back: Render (Docker) · BD: Neon o Render PostgreSQL · Front: Vercel o Netlify |
| Gestión | Trello · Git con ramas feature y PR a `main` |

Paquete base: `co.edu.uniquindio.uqdelivery`.

---

## 3. Estructura del monorepo

```
uqdelivery/
├── CLAUDE.md                  ← este archivo
├── README.md                  ← cómo levantar todo
├── docker-compose.yml         ← PostgreSQL local
├── .gitignore / .gitattributes
├── .github/pull_request_template.md
├── backend/                   ← Spring Boot (abrir esta carpeta en IntelliJ)
├── frontend/                  ← Angular (abrir esta carpeta en VS Code)
└── docs/
    ├── arquitectura.md        ← cómo funciona el back y el front, explicado para el equipo
    ├── guia-historias.md      ← qué archivos crea y toca cada historia, y cómo no pisarse
    ├── api.md                 ← contrato de endpoints (complementa Swagger)
    └── modelo-datos/          ← diagrama entidad-relación (PNG + Mermaid) y su generador
```

Los entregables de Scrum (dailies, burndown, retrospectiva, presentación) no van en el repositorio.

---

## 4. Backend

### 4.1 Arquitectura por capas (NO hexagonal)

Flujo: **front → controller → service → repository / dominio**.

```
backend/src/main/java/co/edu/uniquindio/uqdelivery/
├── controllers/          ← reciben DTOs, validan con @Valid, llaman al service, mapean la respuesta
├── dtos/                 ← al mismo nivel que controllers: solo los usan controllers y mappers
│   ├── request/          ← lo que entra por la API (records con Bean Validation)
│   └── response/         ← lo que sale por la API (records)
├── services/             ← lógica de negocio y orquestación; NO conocen los DTOs
├── repositories/         ← interfaces Spring Data JPA
├── domain/
│   ├── model/            ← entidades JPA (= tablas) y enums, con comportamiento
│   └── datos/            ← records de dominio que no son tablas: resultados que no se guardan (Cotizacion,
│                           ya creado) y, si una historia quiere, entradas con muchos campos (ver 4.2)
└── config/
    ├── mapper/           ← un mapper por dominio (UsuarioMapper, EnvioMapper): clases @Component
    │                       escritas a mano, sin MapStruct
    ├── exception/        ← GlobalExceptionHandler + excepciones propias
    ├── security/         ← SecurityConfig, JwtService, JwtAuthenticationFilter, UsuarioAutenticado
    └── OpenApiConfig.java

backend/src/main/resources/
├── application.yml
└── data.sql              ← datos de prueba; se cargan en cada arranque (ver 4.10)
```

Qué archivos agrega cada historia en estas carpetas: sección 6.0 y `docs/guia-historias.md`.

### 4.2 Regla de DTOs y mappers (obligatoria)

**Entrada (request):**
- El controller recibe el `XxxRequest` y pasa al service **los datos sueltos**:
  `usuarioService.cambiarRol(id, request.rol())`. Por defecto **no hay mapper de request.**
- **Recomendación (no obligatoria):** si son muchos campos o hay listas, el mapper puede convertir el request
  en una **clase intermedia** (`record` en `domain/datos/`, p. ej. `DatosEnvio`) y el service recibe esa clase.
  Lo decide cada historia: no afecta a las demás.
- El service **nunca** recibe ni devuelve DTOs.

**Salida (response):**
- El service devuelve **entidades** (o un record de dominio cuando el resultado no se persiste, p. ej.
  `Cotizacion`, que ya existe en `domain/datos/`).
- El controller usa el mapper para convertirlas en `XxxResponse`.

**Modificaciones y acciones:** el controller pasa el `id` y los datos sueltos; el service busca la entidad,
llama a su método de dominio y guarda.

**Contraseñas:** el service recibe el texto plano, lo encripta con `PasswordEncoder` y solo entonces crea o
modifica la entidad. La entidad solo conoce el hash.

Ejemplo:
```java
@PatchMapping("/{id}/rol")
@PreAuthorize("hasRole('ADMIN')")
public UsuarioResponse cambiarRol(@PathVariable Long id, @Valid @RequestBody CambiarRolRequest request) {
    return usuarioMapper.toResponse(usuarioService.cambiarRol(id, request.rol()));
}
```

### 4.3 Modelo no anémico

- La lógica vive en los services, pero **lo que solo depende de los datos de la propia entidad va como método
  en la entidad** (validaciones de estado, cálculos, transiciones).
- Las entidades **no** dependen de repositories, `PasswordEncoder`, correo ni nada externo.
- **Sin `@Setter` ni `@Data`** en entidades. Usar `@Getter`, constructor `protected` sin argumentos para JPA
  (`@NoArgsConstructor(access = AccessLevel.PROTECTED)`) y constructores de negocio.
- Enums con `@Enumerated(EnumType.STRING)`.
- Dinero y medidas con `BigDecimal` (columna `numeric`), nunca `double`.
- Relaciones `@ManyToOne(fetch = FetchType.LAZY)`.
- Fechas con `@PrePersist` / `@PreUpdate`.

### 4.4 Modelo de datos

El modelo completo (todos los sprints) está en `docs/modelo-datos/diagrama-er.md`, como **diagrama
entidad-relación** con notación pata de gallo: tablas con columnas clave (PK/FK) · nombre · tipo de PostgreSQL,
y líneas de la FK a la PK sin texto. Hay dos versiones, Sprint 1 y completo, cada una como PNG
(`diagrama-er-sprint-1.png`, `diagrama-er.png`) y como Mermaid editable. Las dos salen de
`docs/modelo-datos/generar-diagramas.js`: si el modelo cambia, se edita ese archivo, se regeneran los PNG
y se pega el Mermaid nuevo. Tiene **11 entidades** (cada una es una tabla) y **10 enumeraciones**.

**Entidades:**

| Clase | Tabla | Para qué | Sprint |
|---|---|---|---|
| `Usuario` | `usuarios` | Cuentas y rol | 1 |
| `TokenRecuperacion` | `tokens_recuperacion` | Enlaces de recuperación de contraseña | 1 |
| `ConfiguracionTarifa` | `configuracion_tarifa` | Tarifas y límites del paquete (una fila) | 1 |
| `Envio` | `envios` | Solicitudes de envío | 1 |
| `PerfilRepartidor` | `perfil_repartidor` | Vehículo, placa, disponibilidad (0..1 por usuario) | 2 |
| `Asignacion` | `asignaciones` | Historial de asignación/reasignación de repartidores | 2 |
| `HistorialEstado` | `historial_estados` | Cambios de estado de cada envío | 2 |
| `Novedad` | `novedades` | Imprevistos reportados en ruta | 2 |
| `EvidenciaEntrega` | `evidencias_entrega` | Firma, foto o código al entregar | 2 |
| `Notificacion` | `notificaciones` | Correos/alertas enviados | 2 |
| `UbicacionRepartidor` | `ubicaciones_repartidor` | Puntos de rastreo | 3 |

**Enumeraciones (tipos, estados y listas fijas):**

**Regla:** en el diagrama, toda columna que sea un tipo, un estado o un valor de una lista fija se dibuja
con un **`«enum»` aparte** (caja gris con sus valores), unido a la columna que lo usa. En el código es un
`enum` guardado con `@Enumerated(EnumType.STRING)`, así que **no es una tabla** en la base de datos.
Si algún día el admin debe poder editar una de estas listas, se convierte en entidad y se documenta aquí.

Significado de los valores del Sprint 2:
- `EstadoAsignacion`: `ACTIVA` (la vigente), `RECHAZADA` (el repartidor no la aceptó), `REASIGNADA` (el admin
  la pasó a otro repartidor), `FINALIZADA` (el envío se entregó o se canceló).
- `TipoNotificacion`: `CAMBIO_ESTADO` (HU-12), `CANCELACION` (HU-13), `ASIGNACION` (aviso al repartidor, HU-06),
  `NOVEDAD` (aviso al admin, HU-10).
- `CanalNotificacion`: `CORREO` (Brevo) y `APLICACION` (aviso dentro de la app; usa el campo `leida`).
- `EstadoNotificacion`: `PENDIENTE`, `ENVIADA`, `FALLIDA` y `OMITIDA` (el usuario desactivó sus
  notificaciones, criterio 2 de HU-12).
- `TipoNovedad`: `OTRO` exige describir el caso; la descripción es obligatoria siempre (criterio 2 de HU-10).

| Enum | Lo usa | Valores | Sprint |
|---|---|---|---|
| `Rol` | `Usuario.rol` | `ADMIN`, `EMISOR`, `REPARTIDOR` | 1 |
| `Ciudad` | `Envio.ciudadOrigen`, `Envio.ciudadDestino` | `ARMENIA`, `PEREIRA`, `MANIZALES`, `CALARCA`, `MONTENEGRO` (con nombre, latitud y longitud) | 1 |
| `EstadoEnvio` | `Envio.estado`, `HistorialEstado.estadoAnterior`, `HistorialEstado.estadoNuevo` | `PENDIENTE`, `ASIGNADO`, `EN_CAMINO`, `ENTREGADO`, `CANCELADO` | 1 |
| `TipoVehiculo` | `PerfilRepartidor.tipoVehiculo` | `MOTO`, `BICICLETA`, `CARRO`, `CAMIONETA` | 2 |
| `EstadoAsignacion` | `Asignacion.estado` | `ACTIVA`, `RECHAZADA`, `REASIGNADA`, `FINALIZADA` | 2 |
| `TipoNovedad` | `Novedad.tipo` | `DIRECCION_ERRONEA`, `DESTINATARIO_AUSENTE`, `PAQUETE_DANADO`, `ACCESO_RESTRINGIDO`, `OTRO` | 2 |
| `TipoEvidencia` | `EvidenciaEntrega.tipo` | `FIRMA`, `FOTO`, `CODIGO` | 2 |
| `TipoNotificacion` | `Notificacion.tipo` | `CAMBIO_ESTADO`, `CANCELACION`, `ASIGNACION`, `NOVEDAD` | 2 |
| `CanalNotificacion` | `Notificacion.canal` | `CORREO`, `APLICACION` | 2 |
| `EstadoNotificacion` | `Notificacion.estado` | `PENDIENTE`, `ENVIADA`, `FALLIDA`, `OMITIDA` | 2 |

`Envio` y `Usuario` son las clases centrales: casi todas las demás tienen una asociación con una de ellas.
Los atributos de `Envio` para mapas (`latitudDestino`, `longitudDestino`) se agregan en el Sprint 3.

Decisiones del modelo:
- Las asociaciones van en un solo sentido (`@ManyToOne`): no hay listas de hijos en las entidades; las consultas
  en sentido contrario se hacen con el repository. No se borran envíos ni usuarios, así que no hay borrado en cascada.
- No hay relaciones muchos a muchos directas: envíos ↔ repartidores se resuelve con `Asignacion` y envíos ↔
  estados con `HistorialEstado`, que además guardan datos propios.
- `Envio.repartidor` repite a propósito el repartidor actual que también está en `Asignacion`, para consultarlo
  sin recorrer el historial. En HU-06, asignar o reasignar actualiza los dos.
- `PerfilRepartidor` comparte la llave de `Usuario` (`@OneToOne` con `@MapsId`): como máximo un perfil por usuario.
- `HistorialEstado.estadoAnterior` es nulo en el primer registro de cada envío.
- En la base, atributos en `snake_case` y asociaciones con sufijo `_id` (`emisor` → `emisor_id`).
- Las tablas del Sprint 1 son las del código; los largos de texto de las de los Sprints 2 y 3 que muestra el
  diagrama son una propuesta.

### 4.5 Entidades del Sprint 1

En HU-00 se crean solo estas, **con atributos, getters y constructores, sin métodos de dominio**. Cada
historia agrega a la entidad los métodos que necesite, siguiendo la sección 4.3.

- **`Usuario`** (`usuarios`): id, nombre, correo (único, minúsculas), contrasenaHash, telefono, rol, activo
  (siempre true por ahora), notificacionesActivas (true), fechaRegistro.
- **`TokenRecuperacion`** (`tokens_recuperacion`): id, usuario, token (UUID, único), fechaExpiracion,
  usado, fechaCreacion.
- **`ConfiguracionTarifa`** (`configuracion_tarifa`): **una sola fila (id = 1)**. tarifaBase, precioPorKm,
  precioPorKg, factorRuta (1.4), distanciaMinimaKm, pesoMaximoKg, medidaMaximaCm, actualizadoPor (null),
  fechaActualizacion.
- **`Envio`** (`envios`): id, codigoSeguimiento (único), emisor, repartidor (null hasta HU-06),
  direccionOrigen, ciudadOrigen, direccionDestino, ciudadDestino, nombreDestinatario, telefonoDestinatario,
  correoDestinatario (opcional), descripcion, pesoKg, largoCm, anchoCm, altoCm, distanciaKm, costo, estado,
  fechaCreacion, fechaEntrega (null), fechaActualizacion.

**Enums:**
- `Rol`: `ADMIN`, `EMISOR`, `REPARTIDOR`
- `EstadoEnvio`: `PENDIENTE`, `ASIGNADO`, `EN_CAMINO`, `ENTREGADO`, `CANCELADO`
  (no se cancela desde `EN_CAMINO` ni `ENTREGADO`)
- `Ciudad`: `ARMENIA`, `PEREIRA`, `MANIZALES`, `CALARCA`, `MONTENEGRO`, cada una con nombre, latitud y
  longitud.

### 4.6 Reglas de negocio decididas

**HU-01 · Registro e inicio de sesión**
- Registro público pide: nombre, correo, teléfono, contraseña y confirmación. Todos obligatorios.
- Siempre crea un usuario `EMISOR`. Para tener un repartidor o admin, el admin cambia el rol (HU-02).
- Contraseña segura: mínimo 8 caracteres, al menos una mayúscula, una minúscula y un número.
  Misma regla en front y back.
- Correo único sin distinguir mayúsculas (se guarda en minúsculas). Duplicado → 409.
- Credenciales incorrectas → 401 con el mensaje genérico "Correo o contraseña incorrectos" (no revelar
  si falló el correo o la contraseña).
- Login devuelve `{ token, usuario }`. JWT de **2 horas**, sin refresh token.
- La autoridad del usuario se crea como `"ROLE_" + rol.name()` (necesario para `hasRole('ADMIN')`).
- **HU-01 entrega primero el endpoint de login** (los demás lo necesitan para probar endpoints protegidos).

**HU-02 · Roles**
- El admin lista todos los usuarios (sin paginación en este sprint) y cambia el rol de cualquiera.
- **El admin no puede cambiar su propio rol** (evita quedar sin administradores).
- Usuarios sin rol ADMIN reciben 403.

**HU-03 · Recuperación de contraseña**
- Enlace de **30 minutos y un solo uso**. Pedir uno nuevo invalida los anteriores del usuario.
- Si el correo no existe, responder **igual que si existiera** ("si el correo está registrado, te enviamos
  un enlace") para no revelar qué correos tienen cuenta.
- El enlace apunta al **front**: `${app.frontend-url}/restablecer?token=...`.
- Enlace expirado o usado → se rechaza y se pide uno nuevo.

**HU-04 · Solicitud de envío**
- Formulario con dirección exacta + **ciudad de una lista** para origen y destino, datos del destinatario
  (correo opcional), descripción, peso y dimensiones.
- Valida que el peso y las medidas no superen los límites de `configuracion_tarifa`.
- **El back siempre recalcula el costo** con el servicio de cotización; nunca usa un costo enviado por el front.
- Costo y distancia se guardan en el envío y **no se recalculan después** (si cambian las tarifas, los envíos
  viejos conservan su precio).
- Nace en `PENDIENTE` y devuelve el código de seguimiento.
- Código: `UQD-` + 8 caracteres en mayúscula sin caracteres confusos (sin O, 0, I, 1). Si existe, se genera otro.

**HU-05 · Cotización**
- Distancia **entre ciudades** (no entre direcciones exactas): Haversine × factor de ruta, con mínimo de
  `distanciaMinimaKm` para envíos en la misma ciudad. Decisión documentada.
- `costo = tarifaBase + km × precioPorKm + kg × precioPorKg`, redondeado a la **centena** más cercana, en COP,
  con `RoundingMode.HALF_UP` (12 350 → 12 400; 12 349 → 12 300).
- Fuera de los límites de peso o medidas → error "El paquete no cumple con las políticas de tamaño".
- Las tarifas y límites viven en la tabla `configuracion_tarifa` (no en código ni en `application.yml`),
  porque son decisiones del negocio que el admin gestionará (HU-16).
- El service devuelve el record de dominio `Cotizacion(costo, distanciaKm)` (`domain/datos/`, ya creado en HU-00).

**Flujo de pantallas HU-04 + HU-05:**
1. El emisor llena el formulario.
2. "Cotizar" → `POST /api/envios/cotizacion` → se muestra el costo (componente `resumen-cotizacion` de HU-05).
3. "Confirmar envío" → `POST /api/envios` → el back recalcula y crea.
4. Pantalla de confirmación con el código de seguimiento.
Si el usuario cambia peso, medidas o ciudades después de cotizar, la cotización se borra y debe cotizar de nuevo.

**Regla para distinguir configuración:** si lo cambiaría el dueño del negocio → base de datos
(`configuracion_tarifa`). Si lo cambiaría quien despliega → `application.yml` / variables de entorno.

### 4.7 Endpoints del Sprint 1

| Método | Ruta | Rol | HU |
|---|---|---|---|
| GET | `/api/health` | Público | HU-00 |
| POST | `/api/auth/login` | Público | HU-01 |
| POST | `/api/auth/registro` | Público | HU-01 |
| POST | `/api/auth/recuperar` | Público | HU-03 |
| POST | `/api/auth/restablecer` | Público | HU-03 |
| GET | `/api/usuarios` | ADMIN | HU-02 |
| PATCH | `/api/usuarios/{id}/rol` | ADMIN | HU-02 |
| GET | `/api/ciudades` | Autenticado | HU-04 (lista de ciudades del formulario) |
| GET | `/api/configuracion/politicas` | Autenticado | HU-04 (límites para validar el formulario) |
| POST | `/api/envios/cotizacion` | EMISOR | HU-05 |
| POST | `/api/envios` | EMISOR | HU-04 |

Convenciones: rutas en plural bajo `/api`, nombres en español. Cada historia documenta sus endpoints en
`docs/api.md` (request, response y errores) **antes** de implementarlos.

### 4.8 Errores

Todas las respuestas de error tienen la misma forma (`ApiError`):
```json
{ "status": 400, "message": "Hay campos inválidos", "errors": { "pesoKg": "Debe ser mayor a 0" } }
```
- `errors` solo aparece en errores de validación.
- Excepciones propias en `config/exception/`: `RecursoNoEncontradoException` (404), `ReglaNegocioException`
  (400), `ConflictoException` (409). `AccessDeniedException` → 403. `BadCredentialsException` (login
  fallido) → 401 "Correo o contraseña incorrectos". Cualquier otra → 500 con mensaje genérico (nunca
  exponer trazas).
- Los errores del filtro de seguridad (sin token, token inválido o vencido, rol insuficiente en la URL)
  **no pasan por el `GlobalExceptionHandler`**: `SecurityConfig` define un `AuthenticationEntryPoint` (401) y
  un `AccessDeniedHandler` (403) que escriben el mismo formato `ApiError`.
- Los services lanzan la excepción adecuada; no construyen respuestas HTTP.
- Mensajes de validación **en español** en cada anotación: `@NotBlank(message = "El nombre es obligatorio")`.

### 4.9 Seguridad

- `SecurityConfig`: sesión `STATELESS`, CSRF deshabilitado (API con JWT), CORS desde `app.cors.allowed-origins`,
  `@EnableMethodSecurity` para `@PreAuthorize`.
- Rutas públicas: `/api/auth/**`, `/api/health`, `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**` y
  `/error` (ruta interna de Spring). Todo lo demás requiere token.
- **Roles en cada endpoint:** cada método de controller que no sea para cualquier usuario con sesión lleva
  `@PreAuthorize("hasRole('ADMIN')")` (o `hasAnyRole(...)`), sin el prefijo `ROLE_`. Si se olvida, el endpoint
  queda abierto a cualquier rol.
- **El usuario actual sale del token**, nunca de un `id` en el body o la URL: los services inyectan
  `UsuarioAutenticado` (ya existe en `config/security/`) y llaman a `usuarioAutenticado.obtener()`.
- `PasswordEncoder` = BCrypt.
- **Datos de prueba en `data.sql`**: `admin@uqdelivery.co` (ADMIN), `emisor@uqdelivery.co` (EMISOR),
  `repartidor@uqdelivery.co` (REPARTIDOR) y `carlos@uqdelivery.co` (REPARTIDOR), todos con la contraseña `Clave123*` (en el archivo va su hash BCrypt); la fila de
  `configuracion_tarifa` (base 5000, 200/km, 1000/kg, factor 1.4, mínimo 5 km, máx. 50 kg y 150 cm) y un
  envío en cada estado. Quien agregue datos de prueba los agrega a `data.sql`.
- **Antes de desplegar** hay que cambiar esto: el repo es público y `Clave123*` está escrita aquí, así que
  cualquiera podría entrar como admin a la app desplegada.
- **Inyección SQL:** solo métodos de repository o `@Query` con parámetros. Nunca concatenar texto en consultas.
- **XSS:** longitudes máximas en los DTOs; en Angular nunca `innerHTML` con datos del usuario.

### 4.10 Configuración (`application.yml`)

Un solo archivo, **sin perfiles** (reemplaza el `application.properties` que generó Initializr).
Las variables viven en **un solo `.env` en la raíz** (copia de `.env.example`), que leen `docker-compose.yml`
y Spring (`spring.config.import: optional:file:../.env[.properties]`). En `application.yml` cada valor es
`${VARIABLE:valor_local}`. Por ahora la configuración es solo para local; lo del despliegue se agrega
cuando se despliegue.
Variables: `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`,
`JWT_SECRET`, `JWT_EXP` (120 min), `CORS_ORIGINS`, `FRONTEND_URL`, `MAIL_HOST` (smtp-relay.brevo.com),
`MAIL_PORT` (2525), `MAIL_USER`, `MAIL_PASSWORD` (clave SMTP de Brevo, no API key), `MAIL_FROM`.
`open-in-view: false`. **Nunca** subir credenciales reales.

**Base de datos desde cero en cada arranque:** `ddl-auto: create-drop` crea las tablas a partir de las
entidades al arrancar (y las borra al apagar); luego Spring ejecuta `data.sql`
(`defer-datasource-initialization: true`, `sql.init.mode: always`). Lo que se guarde mientras la app corre
se pierde al reiniciarla. Si una historia cambia una entidad, revisa que `data.sql` siga funcionando.

### 4.11 Pruebas

- Pruebas unitarias con JUnit 5 + Mockito, **sin base de datos**: métodos de dominio y services.
- Meta: 60-70 % de cobertura en el back.
- Cada historia escribe las pruebas de su service **mientras** desarrolla.
- El test `contextLoads` generado por Initializr está deshabilitado (necesita PostgreSQL).
- La cotización es determinista: probarla con valores exactos.

### 4.12 Comandos

```bash
cp .env.example .env             # variables locales (una sola vez)
docker compose up -d              # PostgreSQL local (desde la raíz)
cd backend
./gradlew bootRun                 # arrancar (Windows: .\gradlew.bat bootRun)
./gradlew test                    # pruebas
./gradlew build -x test           # compilar
```

---

## 5. Frontend

### 5.1 Conceptos y estilo

- Angular 22, **componentes standalone**, archivos con el nombre nuevo (`login.ts`, `login.html`, `login.css`).
- Estado con **signals** (`signal`, `computed`); datos entre componentes con `input()` / `output()`.
- Control de flujo con `@if`, `@for` (con `track`), `@empty`. No usar `*ngIf` / `*ngFor`.
- Formularios **reactivos** (`ReactiveFormsModule`, `FormBuilder`). Validación en el cliente con las mismas
  reglas que el back.
- **Angular Material** para todos los componentes visuales: `mat-form-field`, `mat-button`, `mat-table`,
  `mat-sidenav`, `MatSnackBar` para mensajes.
- Crear siempre con el CLI: `ng g c ...`, `ng g s ...`.
- Nadie del equipo ha usado Angular antes: la documentación del front (`docs/arquitectura.md`) explica
  cómo funciona y cómo se organiza en lenguaje sencillo, y `features/inicio/` sirve de ejemplo a copiar.

### 5.2 Estructura

Estructura esperada al terminar el Sprint 1 (lo marcado con HU-0X lo crea esa historia; el detalle de cada
archivo está en `docs/guia-historias.md`):

```
frontend/src/
├── material-theme.scss           ← HU-00: tema de Angular Material (colores de todos los componentes)
├── styles.css                    ← HU-00: variables --uqd-..., clases .pagina, .chip-estado
├── environments/environment.ts   ← HU-00: apiUrl
└── app/
    ├── app.config.ts             ← HU-00: provideRouter, provideHttpClient(withInterceptors(...))
    ├── app.routes.ts             ← HU-00 (HU-02 y HU-04 descomentan su línea)
    ├── core/                     ← HU-00 (compartido: avisar al equipo antes de cambiarlo)
    │   ├── auth/                 ← auth.service.ts (signals usuario, token, rol), auth.guard.ts, role.guard.ts
    │   ├── interceptors/         ← auth.interceptor.ts (agrega Bearer), error.interceptor.ts
    │   │                           (401 → cerrar sesión e ir a login, excepto en /api/auth/**)
    │   ├── models/               ← rol.ts, usuario.models.ts (UsuarioResponse, LoginResponse), api-error.ts
    │   └── layout/               ← public-layout, private-layout (menú lateral con OPCIONES por rol), navbar, logo
    ├── shared/
    │   ├── components/           ← mensaje-error, cargando
    │   └── validators/           ← contrasenas-iguales (HU-00), contrasena-segura (HU-01)
    └── features/
        ├── inicio/               ← HU-00: página de bienvenida (ejemplo a copiar)
        ├── auth/                 ← auth.routes.ts (HU-01 y HU-03 agregan sus rutas)
        │   ├── pages/            ← login y registro (HU-01) · solicitar-recuperacion y restablecer-contrasena (HU-03)
        │   ├── services/         ← auth-api.service.ts (HU-01) · recuperacion.service.ts (HU-03)
        │   └── models/           ← auth.models.ts (HU-01) · recuperacion.models.ts (HU-03)
        ├── usuarios/             ← HU-02: usuarios.routes.ts, pages/gestion-usuarios, services, models
        └── envios/               ← envios.routes.ts (HU-04)
            ├── pages/            ← nuevo-envio y envio-creado (HU-04)
            ├── components/       ← resumen-cotizacion (HU-05, se inserta en nuevo-envio)
            ├── services/         ← envios.service.ts (HU-04) · cotizacion.service.ts (HU-05)
            └── models/           ← envio.models.ts (HU-04) · cotizacion.models.ts (HU-05)
```

Cada feature tiene sus carpetas `pages/`, `components/`, `services/`, `models/` y su propio `xxx.routes.ts`.
Cuando dos historias comparten una feature, cada una tiene **sus propios archivos** de servicio y modelos, para
no editar el mismo archivo a la vez.

### 5.3 Reglas

- **Los componentes nunca llaman a `HttpClient` directamente**: siempre a través de un servicio de su feature.
- Los modelos del front son `interface` espejo de los DTOs del back, **con los mismos nombres**
  (`EnvioRequest`, `UsuarioResponse`). `LocalDateTime` llega como `string`; enums como uniones de texto.
- Las URLs salen de `environment.apiUrl` (`src/environments/environment.ts`). Escribir siempre
  `` `${environment.apiUrl}/usuarios` ``: el interceptor solo agrega el token a las URLs que empiezan así.
  El `environment.prod.ts` (URL del back desplegado) y su reemplazo en `angular.json` se agregan al desplegar.
- Patrón de página: signals `cargando`, `error` y el resultado; `.subscribe({ next, error })` mostrando
  `err.error.message` del `ApiError`.
- Mientras un endpoint no exista, el servicio devuelve datos falsos que cumplen el contrato con
  `of({...}).pipe(delay(800))` y la llamada real comentada. Al conectar, solo cambia el servicio.
- **Colores:** el tema está en `src/material-theme.scss` (primario `cyan`, terciario `azure`) y los colores
  propios en `src/styles.css` (`--uqd-...`: fondo, menú lateral oscuro, estados de envío). En los `.css` de
  las pantallas **nunca** se escriben colores a mano: siempre `var(--mat-sys-...)` o `var(--uqd-...)`.
  Las páginas usan las clases globales `.pagina` / `.pagina-angosta`, `.encabezado-pagina`, `.titulo-pagina`,
  `.subtitulo-pagina`, y los estados de envío `.chip-estado` + `.estado-<ESTADO>`.
- Dependencias entre carpetas: `features` usa `core` y `shared`; `shared` no usa `features`; una feature no
  importa archivos de otra.
- Mientras HU-01 no termine el login real, la página de login tiene botones de desarrollo
  "Entrar como ADMIN / EMISOR / REPARTIDOR" que simulan la sesión.

### 5.4 Rutas

- Layout público (sin sesión): `/login`, `/registro`, `/recuperar`, `/restablecer`.
- Layout privado bajo `/app` con `authGuard`: `/app/inicio`, `/app/usuarios` (`roleGuard('ADMIN')`),
  `/app/envios/nuevo` (`roleGuard('EMISOR')`).
- Cada feature se registra con **una línea** en `app.routes.ts` que carga su `xxx.routes.ts` con `loadChildren`.
  Las de HU-02 y HU-04 ya están escritas como comentario: se descomentan y se importa `roleGuard`.
  Las rutas públicas de `auth` van en `features/auth/auth.routes.ts`, que ya está registrado.
- El menú lateral cambia según el rol: sus opciones están en `OPCIONES` de `core/layout/private-layout/private-layout.ts`
  (Inicio, Usuarios y Nuevo envío ya existen). Una pantalla nueva que deba ir en el menú agrega ahí una línea.
- Los guards solo esconden pantallas; la seguridad real está en el back (`@PreAuthorize`). Van siempre los dos.
- Navegar con `routerLink` o `Router.navigate`, nunca con `href`.

### 5.5 Comandos

```bash
cd frontend
npm install
ng serve        # http://localhost:4200
ng build
```

---

## 6. Trabajo en equipo

- **Una historia por persona** (HU-00 a HU-05). Cada quien hace back, front, pruebas y documentación de su
  historia en **una sola rama**. Los entregables del sprint (sección 6.2) también se reparten.
- Ramas: `feature/HU-0X-descripcion` desde `main`. **Todo va a `main` por Pull Request**, sin rama `develop`.
- Commits con prefijo y sin la historia entre paréntesis: `feat: formulario de login con validaciones`,
  `fix: ...`, `chore: ...`, `docs: ...`. La historia ya se ve en el nombre de la rama.
- Hacer `git pull origin main` en la rama al menos una vez al día.
- **Archivos compartidos** (los usan todas las historias): `build.gradle`, `package.json`, `app.config.ts`,
  `core/`, `config/security/`, `styles.css`, `material-theme.scss`. Si una historia necesita cambiarlos, lo avisa al equipo antes y
  hace ese cambio en un PR pequeño y aparte, para que nadie se lleve sorpresas. Nadie agrega dependencias sin
  avisar.
- `app.routes.ts`: cada historia agrega (o descomenta) solo la línea que apunta a su `xxx.routes.ts`.
- HU-01 y HU-03 comparten `features/auth/` y la ruta `/api/auth` (cada una con su controller: `AuthController`
  y `RecuperacionController`). HU-04 y HU-05 comparten `features/envios/` y la ruta `/api/envios`
  (`EnvioController` y `CotizacionController`); HU-05 hace el componente `resumen-cotizacion`, que HU-04 inserta
  en su formulario.
- Sin integración continua: quien revisa el PR descarga la rama y comprueba que el back arranca y el front compila.

### 6.0 Qué toca cada historia

Resumen; el detalle (archivos esperados del back y del front y puntos delicados) está en
`docs/guia-historias.md`. Es una guía para trabajar igual y no pisarse, no una camisa de fuerza: si una historia
necesita otro archivo u otro nombre, lo hace y avisa si afecta a alguien.

| HU | Back (archivos propios) | Front (carpeta) | Archivos compartidos que toca |
|---|---|---|---|
| HU-01 | `AuthController`, `AuthService`, `UsuarioMapper`, `UsuarioResponse`, `LoginResponse` | `features/auth/` (login, registro) | `auth.routes.ts`, `shared/validators/contrasena-segura` |
| HU-02 | `UsuarioController`, `UsuarioService`, `Usuario.cambiarRol()` | `features/usuarios/` | `app.routes.ts` (su línea) |
| HU-03 | `RecuperacionController`, `RecuperacionService`, `CorreoService` | `features/auth/` (recuperar, restablecer) | `auth.routes.ts`, enlace en la página de login (con HU-01) |
| HU-04 | `EnvioController`, `CiudadController`, `ConfiguracionController`, `EnvioService`, `EnvioMapper` | `features/envios/` (páginas) | `app.routes.ts` (su línea) |
| HU-05 | `CotizacionController`, `CotizacionService`, `CotizacionMapper` (usa el record `Cotizacion`, ya creado) | `features/envios/components/resumen-cotizacion` | — |

Acuerdos del primer día (una historia necesita algo de otra):
- HU-01 sube primero el login, `UsuarioResponse` y `UsuarioMapper` (los usa HU-02 y todos para probar).
- HU-04 y HU-05 acuerdan la firma de `CotizacionService.cotizar(...)` y los `input()`/`output()` de
  `resumen-cotizacion`.
- HU-01 y HU-03 acuerdan el enlace "¿Olvidaste tu contraseña?" en el login.

### Definición de terminado

Una historia está terminada solo si:
- [ ] Cumple sus dos criterios de validación (Dado/Cuando/Entonces).
- [ ] Tiene validación en front y en back con las mismas reglas.
- [ ] Sus services tienen pruebas unitarias.
- [ ] Sus endpoints están en Swagger y en `docs/api.md`.
- [ ] El PR fue revisado y mergeado a `main`.
- [ ] Funciona en el entorno desplegado.

### 6.1 Scrum

- Gestión en Trello: `Por hacer → En progreso → En revisión → Terminado`, una tarjeta por HU con subtareas.
- **4 dailies en el sprint** (2 por semana). En cada una se toma evidencia (captura o foto) y cada integrante
  llena la plantilla con tres preguntas:
  1. ¿Qué hice desde la última reunión? (*What did you do yesterday?*)
  2. ¿Qué haré hoy? (*What are you doing today?*)
  3. ¿Hay algo que me bloquee? (*Is there anything blocking you?*)
- Las dailies, el burndown (SP restantes por día) y la retrospectiva se llevan fuera del repositorio.

### 6.2 Presentación del sprint

| Diapositiva | Contenido |
|---|---|
| 0 | Nombre del equipo, nombre y foto de los integrantes |
| 1 | Framework, lenguaje, entorno y herramientas de desarrollo |
| 2 | Backlog completo con SP · historias del sprint actual con SP |
| 3 | Gráfica burndown |
| 4 | Daily Scrum (las 4 reuniones con evidencia) |
| 5 | Capturas del trabajo de cada integrante en Trello |
| 6 | Evidencia en Git (ramas, commits, PR) |
| 7 | Retrospectiva: qué salió bien, qué salió mal, qué se va a mejorar |
| 8 | Demostración de la aplicación |

---

## 7. Despliegue

Todavía no está hecho; esto es lo decidido para cuando se haga.

- Back en Render con un `backend/Dockerfile` (multi-etapa, Gradle → JRE 21, aún no creado). Variables de
  entorno en el panel. Agregar `TZ=America/Bogota`.
- Front: crear `environment.prod.ts` con la URL del back desplegado y su reemplazo en `angular.json`.
- Render gratis se suspende sin uso: abrir la app un par de minutos antes de cualquier demo.
- Front en Vercel o Netlify con regla de reescritura de todas las rutas a `index.html` (si no, recargar en
  `/app/usuarios` da 404).
- Correos con Brevo por el puerto 2525.

---

## 8. Instrucciones para agentes de IA

- Respetar estrictamente la arquitectura por capas y la regla de DTOs de la sección 4.2.
- No agregar dependencias (Gradle o npm) sin que el usuario lo pida explícitamente.
- No modificar `core/`, `config/security/`, `app.config.ts`, `build.gradle` ni `package.json` salvo en tareas de HU-00.
- Antes de crear un endpoint, revisar que coincida con la tabla 4.7 y con `docs/api.md`.
- Al trabajar en una historia, seguir los archivos y nombres de `docs/guia-historias.md` salvo que el usuario
  pida otra cosa.
- Escribir pruebas unitarias para la lógica nueva de services y entidades.
- Mensajes al usuario, nombres de dominio y comentarios en español.
- La documentación va dirigida a un equipo que no conoce Angular: explicar el porqué y el cómo en lenguaje
  sencillo, sin pretender explicar cada línea de código.
- Si una tarea contradice una decisión de este archivo, preguntar antes de cambiarla.

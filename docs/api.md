# Contrato de la API

Este documento describe cada endpoint: qué recibe, qué devuelve y qué errores puede dar. Complementa a
Swagger (`/swagger-ui.html`). **Cada historia documenta aquí sus endpoints antes de programarlos**, para que
front y back trabajen con el mismo contrato.

La lista de endpoints del Sprint 1 está en la sección 4.7 de `CLAUDE.md`.

## Convenciones

- Base: `http://localhost:8080/api`.
- Rutas en plural y en español: `/api/usuarios`, `/api/envios`.
- JSON en el cuerpo de las peticiones y las respuestas.
- Fechas (`LocalDateTime`) como texto ISO: `"2026-10-21T10:30:00"`.
- Enums como texto: `"ADMIN"`, `"PENDIENTE"`.
- Dinero en pesos colombianos (COP) y medidas como números: `12400`, `2.5`.

## Autenticación

Las rutas protegidas piden el token JWT del login en el encabezado:

```
Authorization: Bearer <token>
```

- El token dura 2 horas. No hay refresh token: al vencer, se inicia sesión otra vez.
- Rutas públicas (sin token): `/api/auth/**` y `/api/health`.
- En el front no hay que agregar el encabezado a mano: lo hace `auth.interceptor.ts`.
- En Swagger: botón **Authorize**, pegar el token (sin la palabra `Bearer`).

## Errores

Todas las respuestas de error tienen la misma forma (`ApiError`):

```json
{ "status": 400, "message": "Hay campos inválidos", "errors": { "pesoKg": "Debe ser mayor a 0" } }
```

`errors` solo aparece en errores de validación. En el front se muestra `err.error.message`.

| Código | Cuándo | `message` |
|---|---|---|
| 400 | Campos inválidos (`@Valid`) | "Hay campos inválidos" + `errors` por campo |
| 400 | Cuerpo JSON mal formado | "El cuerpo de la petición no es válido" |
| 400 | Regla de negocio (`ReglaNegocioException`) | El mensaje de la regla |
| 401 | Sin token, token inválido o vencido | "Debes iniciar sesión para continuar" |
| 401 | Login con correo o contraseña incorrectos | "Correo o contraseña incorrectos" |
| 403 | El rol no tiene permiso | "No tienes permiso para realizar esta acción" |
| 404 | No existe (`RecursoNoEncontradoException`) o la ruta no existe | El mensaje de la excepción / "La ruta solicitada no existe" |
| 409 | Conflicto con datos existentes (`ConflictoException`) | El mensaje de la excepción |
| 500 | Error no previsto | "Ocurrió un error inesperado. Intenta de nuevo más tarde" |

## Modelos compartidos

### `UsuarioResponse`

Lo usan varias historias (login, listado de usuarios, cambio de rol). El front ya tiene la interfaz en
`core/models/usuario.models.ts`. **Propuesta de HU-00: HU-01 la confirma o ajusta al crear el DTO.**

```json
{
  "id": 1,
  "nombre": "Administrador",
  "correo": "admin@uqdelivery.co",
  "telefono": "3000000001",
  "rol": "ADMIN",
  "fechaRegistro": "2026-10-21T10:30:00"
}
```

### `LoginResponse`

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9...", "usuario": { "...": "UsuarioResponse" } }
```

---

## Endpoints

### HU-00 · `GET /api/health`

Comprueba que la API responde. **Público.**

**Response 200**

```json
{ "estado": "UP" }
```

**Errores:** ninguno propio.

---
## HU-01 · Registro e inicio de sesión seguro

### 1. Registrar usuario
* **Método:** `POST`
* **Ruta:** `/api/auth/registro`
* **Acceso:** Público
* **Descripción:** Permite registrar un nuevo usuario en el sistema. Siempre asigna por defecto el rol de `EMISOR` y almacena el correo en minúsculas.

**Request (`RegistroRequest`):**
```json
{
  "nombre": "Juan Pérez",
  "correo": "juan@uqdelivery.co",
  "telefono": "3001234567",
  "contrasena": "Clave123*",
  "confirmarContrasena": "Clave123*"
}
```

**Response 201**(`UsuarioResponse`)
```json
{
"id": 1,
"nombre": "Juan Pérez",
"correo": "juan@uqdelivery.co",
"telefono": "3001234567",
"rol": "EMISOR",
"fechaRegistro": "2026-10-27T22:30:00"
}
```
## 2. Iniciar sesión
* **Método:** `POST`
* **Ruta:** `/api/auth/login`
* **Acceso:** Público
* **Descripción:** Autentica al usuario con sus credenciales y retorna un token JWT con una vigencia de 2 horas[cite: 1].

**Request (`LoginRequest`):**
```json
{
"correo": "juan@uqdelivery.co",
"contrasena": "Clave123*"
}
```
**Response 200(`LoginResponse`):**
```json
{
"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
"usuario": {
"id": 1,
"nombre": "Juan Pérez",
"correo": "juan@uqdelivery.co",
"telefono": "3001234567",
"rol": "EMISOR",
"fechaRegistro": "2026-10-27T22:30:00"
}
}
```
### HU-05 · `POST /api/envios/cotizacion`

Calcula el costo y la distancia estimada de un envío sin guardar nada. **Rol:** EMISOR.

**Request** (`CotizacionRequest`)

   ```json
   { "ciudadOrigen": "ARMENIA", "ciudadDestino": "PEREIRA",
     "pesoKg": 2.5, "largoCm": 35, "anchoCm": 25, "altoCm": 5 }
   ```

**Response 200** (`CotizacionResponse`)

   ```json
   { "costo": 16200, "distanciaKm": 43.56 }
   ```

**Errores**

| Código | Cuándo | message |
   |---|---|---|
| 400 | Campos nulos o ≤ 0, ciudad inexistente | "Hay campos inválidos" / "El cuerpo de la petición no es válido" |
| 400 | Fuera de límites de peso o medidas | "El paquete no cumple con las políticas de tamaño" |
| 401 | Sin token | "Debes iniciar sesión para continuar" |
| 403 | Rol distinto de EMISOR | "No tienes permiso para realizar esta acción" |

---

### HU-04 · `GET /api/ciudades`

Lista de ciudades donde opera UQDelivery, para los selectores de origen y destino. **Rol:** Autenticado.

**Response 200** (`CiudadResponse[]`)

```json
[
  { "codigo": "ARMENIA", "nombre": "Armenia" },
  { "codigo": "PEREIRA", "nombre": "Pereira" },
  { "codigo": "MANIZALES", "nombre": "Manizales" },
  { "codigo": "CALARCA", "nombre": "Calarcá" },
  { "codigo": "MONTENEGRO", "nombre": "Montenegro" }
]
```

`codigo` es el valor que se envía en `ciudadOrigen` / `ciudadDestino`; `nombre` es el que se muestra.

**Errores:** 401 sin token.

---

### HU-04 · `GET /api/configuracion/politicas`

Límites del paquete para validar el formulario en el front. Salen de la tabla `configuracion_tarifa`.
**Rol:** Autenticado.

**Response 200** (`PoliticasResponse`)

```json
{ "pesoMaximoKg": 50, "medidaMaximaCm": 150 }
```

`medidaMaximaCm` es el máximo para cada medida (largo, ancho y alto).

**Errores**

| Código | Cuándo | message |
|---|---|---|
| 401 | Sin token | "Debes iniciar sesión para continuar" |
| 404 | No existe la fila de configuración | "No hay configuración de tarifas" |

---

### HU-04 · `POST /api/envios`

Crea una solicitud de envío. El emisor sale del token, el back **recalcula siempre el costo** con la cotización
de HU-05 (no se envía costo), el envío nace en `PENDIENTE` y se devuelve su código de seguimiento.
**Rol:** EMISOR.

**Request** (`EnvioRequest`)

```json
{
  "direccionOrigen": "Carrera 14 # 20-35", "ciudadOrigen": "ARMENIA",
  "direccionDestino": "Calle 19 # 8-40", "ciudadDestino": "PEREIRA",
  "nombreDestinatario": "Andrés López", "telefonoDestinatario": "3151112233",
  "correoDestinatario": "andres.lopez@correo.co",
  "descripcion": "Documentos en sobre de manila",
  "pesoKg": 2.5, "largoCm": 35, "anchoCm": 25, "altoCm": 5
}
```

| Campo | Tipo | Reglas |
|---|---|---|
| direccionOrigen, direccionDestino | string | Obligatorio, máx. 200 caracteres |
| ciudadOrigen, ciudadDestino | string | Obligatorio, un `codigo` de `GET /api/ciudades` |
| nombreDestinatario | string | Obligatorio, máx. 100 caracteres |
| telefonoDestinatario | string | Obligatorio, de 7 a 15 dígitos |
| correoDestinatario | string | **Opcional**, correo válido, máx. 150 caracteres |
| descripcion | string | Obligatorio, máx. 500 caracteres |
| pesoKg, largoCm, anchoCm, altoCm | number | Obligatorio, mayor a 0, máx. 2 decimales, sin superar las políticas |

**Response 201** (`EnvioResponse`)

```json
{
  "id": 6, "codigoSeguimiento": "UQD-7KQ2M9XA",
  "direccionOrigen": "Carrera 14 # 20-35", "ciudadOrigen": "ARMENIA",
  "direccionDestino": "Calle 19 # 8-40", "ciudadDestino": "PEREIRA",
  "nombreDestinatario": "Andrés López", "telefonoDestinatario": "3151112233",
  "correoDestinatario": "andres.lopez@correo.co", "descripcion": "Documentos en sobre de manila",
  "pesoKg": 2.5, "largoCm": 35, "anchoCm": 25, "altoCm": 5,
  "distanciaKm": 43.56, "costo": 16200, "estado": "PENDIENTE",
  "fechaCreacion": "2026-10-29T10:30:00"
}
```

Código de seguimiento: `UQD-` + 8 caracteres en mayúscula, sin O, 0, I ni 1.

**Errores**

| Código | Cuándo | message |
|---|---|---|
| 400 | Campos vacíos o inválidos | "Hay campos inválidos" + `errors` por campo |
| 400 | Ciudad inexistente o JSON mal formado | "El cuerpo de la petición no es válido" |
| 400 | Fuera de límites de peso o medidas | "El paquete no cumple con las políticas de tamaño" |
| 401 | Sin token | "Debes iniciar sesión para continuar" |
| 403 | Rol distinto de EMISOR | "No tienes permiso para realizar esta acción" |

<!--
Plantilla para cada endpoint nuevo (copiar y llenar):

### HU-0X · `MÉTODO /api/ruta`

Qué hace. **Rol:** ADMIN | EMISOR | REPARTIDOR | Autenticado | Público.

**Request** (`NombreRequest`)

```json
{ "campo": "valor" }
```

| Campo | Tipo | Reglas |
|---|---|---|
| campo | string | Obligatorio, máx. 100 caracteres |

**Response 200/201** (`NombreResponse`)

```json
{ "campo": "valor" }
```

**Errores**

| Código | Cuándo | message |
|---|---|---|
| 400 | ... | ... |
-->

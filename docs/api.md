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

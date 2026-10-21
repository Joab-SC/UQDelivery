# UQDelivery

Aplicación web de gestión de envíos y entregas de paquetes en entorno urbano.
Proyecto de Ingeniería de Sistemas · Universidad del Quindío.

- **Back:** Spring Boot 4 (Java 21, Gradle) · API REST en `backend/`
- **Front:** Angular 22 + Angular Material · SPA en `frontend/`
- **Base de datos:** PostgreSQL 17 con Docker Compose

Las decisiones del proyecto (arquitectura, reglas de negocio, forma de trabajo) están en [CLAUDE.md](CLAUDE.md).

## Requisitos

- Java 21
- Node.js 24 y npm
- Docker Desktop (para PostgreSQL)

## Cómo levantar todo en local

### 1. Variables de entorno (una sola vez)

En la raíz del repo:

```bash
cp .env.example .env
```

Los valores del ejemplo ya sirven en local. Si tienes otro PostgreSQL instalado y ocupa el puerto 5432,
cambia `POSTGRES_PORT=5433` en tu `.env`.

### 2. Base de datos

En la raíz del repo:

```bash
docker compose up -d
```

### 3. Back

Abre la carpeta `backend/` en IntelliJ (no la raíz del repo) o, desde una terminal:

```bash
cd backend
./gradlew bootRun
```

En Windows (PowerShell): `.\gradlew.bat bootRun`.

- API: <http://localhost:8080/api>
- Comprobar que responde: <http://localhost:8080/api/health>
- Swagger UI: <http://localhost:8080/swagger-ui.html>

**Cada vez que arranca, el back borra y crea la base desde cero** y carga los datos de prueba de
`backend/src/main/resources/data.sql`. Lo que guardes mientras la app corre se pierde al reiniciarla.

Usuarios de prueba, todos con la contraseña `Clave123*`:

| Correo | Rol |
|---|---|
| `admin@uqdelivery.co` | ADMIN |
| `emisor@uqdelivery.co` | EMISOR |
| `repartidor@uqdelivery.co` | REPARTIDOR |
| `carlos@uqdelivery.co` | REPARTIDOR |

También trae la configuración de tarifas y un envío en cada estado.

### 4. Front

```bash
cd frontend
npm install
ng serve
```

Abrir <http://localhost:4200>. Mientras HU-01 no termine el login real, la pantalla de login tiene botones
"Entrar como ADMIN / EMISOR / REPARTIDOR" para simular una sesión.

## Pruebas

```bash
cd backend
./gradlew test
```

## Documentación

| Documento | Qué tiene |
|---|---|
| [CLAUDE.md](CLAUDE.md) | Fuente de verdad: decisiones, reglas de negocio, endpoints, forma de trabajo |
| [docs/arquitectura.md](docs/arquitectura.md) | Cómo está organizado el back y el front, explicado para quien no conoce Angular |
| [docs/guia-historias.md](docs/guia-historias.md) | Qué archivos crea y toca cada historia, y cómo no pisarse |
| [docs/api.md](docs/api.md) | Contrato de cada endpoint (request, response y errores) |
| [docs/modelo-datos/diagrama-er.md](docs/modelo-datos/diagrama-er.md) | Diagrama entidad-relación (Sprint 1 y completo) |

Los criterios de validación de cada historia están en su tarjeta de Trello.

## Forma de trabajo

- Una rama por historia: `feature/HU-0X-descripcion`, desde `main`, y Pull Request a `main`.
- Commits: `feat: ...`, `fix: ...`, `chore: ...`, `docs: ...`.
- Detalles en la sección 6 de [CLAUDE.md](CLAUDE.md).

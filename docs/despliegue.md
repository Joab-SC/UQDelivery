# Despliegue

La app desplegada son tres piezas, igual que en local, cada una en un servicio gratis:

| Pieza | En local | Desplegada | URL |
|---|---|---|---|
| Base de datos | PostgreSQL en Docker | **Neon** | (solo la usa el back) |
| Back (Spring) | `gradlew bootRun` | **Render** | `https://uqdelivery-api.onrender.com` |
| Front (Angular) | `ng serve` | **Vercel** | `https://uqdelivery.vercel.app` |

El front le pide datos al back y el back lee y escribe en la base, igual que en local. Solo cambian las
direcciones: en vez de `localhost`, cada pieza usa la URL de internet de la otra.

## Cómo se actualiza

Render y Vercel vigilan la rama `main` de GitHub. **Cada vez que se mergea un PR a `main`, vuelven a
construir y publicar la app solos**, en unos minutos. Nadie tiene que hacer nada a mano.

- Si el back no compila o una prueba falla, Render no lo publica y **sigue funcionando la versión anterior**.
- Si el back compila pero no arranca (no responde en `/api/health`), Render tampoco lo cambia.
- Las demás ramas no se despliegan: lo que no está en `main` no está en internet.

## Qué pasa con los datos

- La base **guarda lo que la gente crea** (usuarios, envíos...), aunque el back se reinicie.
- La primera vez, el back crea las tablas y mete los datos de prueba de `data.sql`.
- En cada arranque vuelve a ejecutar `data.sql`, pero cada `INSERT` termina en `ON CONFLICT ... DO NOTHING`
  ("si ya existe, no hagas nada"), así que no duplica nada. **Quien agregue datos a `data.sql` debe hacer lo mismo.**
- Si una historia agrega tablas o columnas, el back las crea solo al desplegarse. Si renombra una columna o
  agrega una obligatoria a una tabla que ya tiene filas, hay que borrar el esquema en Neon (ver al final) y
  el back lo vuelve a crear con los datos de prueba (se pierde lo que se haya creado).

## Cosas a saber

- **Render gratis se duerme** tras unos 15 minutos sin visitas y tarda 1-2 minutos en despertar. Antes de una
  demo, abrir la app un par de minutos antes.
- **Usuarios de prueba**: emisor y repartidores tienen `Clave123*`. La clave del admin es distinta (se cambió
  en la base porque el repo es público); pedírsela a Joab.
- **Correo de recuperación**: necesita la cuenta de Brevo (variables `MAIL_*`). Sin ella, todo funciona menos
  el envío del correo.

---

## Cómo se creó (paso a paso)

Lo que está en el repo y prepara el despliegue:

| Archivo | Para qué |
|---|---|
| `backend/Dockerfile` | Receta para que Render compile (con pruebas) y arranque el back |
| `backend/src/main/resources/application.yml` | Lee `DB_URL`, `DDL_AUTO` y `PORT` del despliegue |
| `frontend/src/environments/environment.prod.ts` | URL del back desplegado (la usa `ng build`) |
| `frontend/vercel.json` | Carpeta que publica Vercel, solo despliega `main` y manda todas las rutas a `index.html` |

### 1. Base de datos en Neon

1. Entrar a <https://neon.tech> con GitHub y crear un proyecto `uqdelivery`, Postgres 17, región
   **AWS US East (Ohio)**.
2. En **Connect**, **desactivar "Connection pooling"** y copiar los datos de la conexión: host, base,
   usuario y contraseña.
3. Armar la URL JDBC (va en Render como `DB_URL`):
   `jdbc:postgresql://<host>/<base>?sslmode=require`
   Por ejemplo: `jdbc:postgresql://ep-algo-123456.us-east-2.aws.neon.tech/neondb?sslmode=require`.
   Usuario y contraseña **no** van en la URL: van aparte en `POSTGRES_USER` y `POSTGRES_PASSWORD`.

### 2. Back en Render

1. Entrar a <https://render.com> con GitHub → **New → Web Service** → elegir el repo.
2. Configurar:
   - **Name:** `uqdelivery-api` (da la URL `uqdelivery-api.onrender.com`)
   - **Branch:** `main`
   - **Region:** Ohio (la misma zona que Neon)
   - **Root Directory:** `backend`
   - **Runtime / Language:** Docker (encuentra el `Dockerfile` solo)
   - **Instance type:** Free
   - **Health Check Path** (en *Advanced*): `/api/health`
3. Variables de entorno (*Environment*):

   | Variable | Valor |
   |---|---|
   | `DB_URL` | La URL JDBC de Neon (paso 1) |
   | `POSTGRES_USER` | Usuario de Neon |
   | `POSTGRES_PASSWORD` | Contraseña de Neon |
   | `DDL_AUTO` | `update` |
   | `JWT_SECRET` | Texto aleatorio largo (64+ caracteres). Render lo genera con *Generate* |
   | `CORS_ORIGINS` | `https://uqdelivery.vercel.app` |
   | `FRONTEND_URL` | `https://uqdelivery.vercel.app` |
   | `MAIL_USER`, `MAIL_PASSWORD`, `MAIL_FROM` | De Brevo, cuando exista la cuenta |

   `PORT` no se pone: Render la asigna sola. `MAIL_MODO_CONSOLA` **nunca** se pone en Render.
4. **Create Web Service**. La primera vez tarda varios minutos (descarga Gradle y corre las pruebas).
5. Comprobar: `https://uqdelivery-api.onrender.com/api/health` responde y
   `https://uqdelivery-api.onrender.com/swagger-ui.html` abre.

### 3. Front en Vercel

1. Entrar a <https://vercel.com> con GitHub → **Add New → Project** → importar el repo.
2. **Project Name:** `uqdelivery` (da la URL `uqdelivery.vercel.app`).
3. **Root Directory:** `frontend`. Lo demás lo toma de `vercel.json`.
4. **Deploy**. Al terminar, abrir la URL, entrar con `emisor@uqdelivery.co` y recargar la página estando en
   una ruta interna (no debe dar 404).

Si Vercel o Render dan otra URL (porque el nombre estaba ocupado), cambiar `environment.prod.ts` y las
variables `CORS_ORIGINS` y `FRONTEND_URL` de Render, y la tabla de arriba.

### 4. Cambiar la clave del admin

Desde el **SQL Editor** de Neon, con el hash BCrypt de la clave nueva:

```sql
UPDATE usuarios SET contrasena_hash = '<hash BCrypt de la clave nueva>' WHERE correo = 'admin@uqdelivery.co';
```

`data.sql` no la vuelve a cambiar, porque el usuario ya existe (`ON CONFLICT DO NOTHING`).

### Borrar la base para empezar de cero

Solo si un cambio de tablas lo exige (se pierde todo lo creado). En el SQL Editor de Neon:

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```

Luego, en Render, **Manual Deploy → Restart service**. El back crea las tablas y los datos de prueba, y hay
que repetir el paso 4.

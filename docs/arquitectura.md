# Arquitectura

Cómo está organizado UQDelivery y por qué. Las reglas obligatorias están en `CLAUDE.md`; aquí se explican
con ejemplos para que cualquiera del equipo pueda empezar su historia. Qué archivos crea y toca cada historia
está en [guia-historias.md](guia-historias.md).

La aplicación tiene dos partes separadas que solo se hablan por HTTP:

```
Navegador (Angular, puerto 4200)  ──HTTP + JSON──▶  API REST (Spring Boot, puerto 8080)  ──▶  PostgreSQL
```

---

## Back (Spring Boot)

### Capas

Cada petición recorre las mismas capas, siempre en este orden:

```
controller  →  service  →  repository / entidad
```

| Capa | Carpeta | Qué hace | Qué NO hace |
|---|---|---|---|
| Controller | `controllers/` | Recibe la petición, valida el DTO con `@Valid`, llama al service y convierte el resultado en DTO de respuesta con el mapper | Lógica de negocio |
| DTO | `dtos/request`, `dtos/response` | `record` con los datos que entran y salen por la API | Llegar al service |
| Mapper | `config/mapper/` | Convierte entidades en `XxxResponse` (y, si la historia quiere, requests muy grandes en records de `domain/datos/`) | Lógica de negocio |
| Service | `services/` | Lógica de negocio: busca, valida reglas, guarda, lanza excepciones | Conocer los DTOs o construir respuestas HTTP |
| Repository | `repositories/` | Consultas a la base de datos (Spring Data JPA) | Lógica |
| Entidad | `domain/model/` | Una clase por tabla, con sus datos y los métodos que solo dependen de sus propios datos | Usar repositories, correo u otros servicios |

**Ejemplo completo (HU-02, cambiar rol):**

```java
// Controller: recibe el DTO y pasa los datos sueltos al service
@PatchMapping("/{id}/rol")
@PreAuthorize("hasRole('ADMIN')")
public UsuarioResponse cambiarRol(@PathVariable Long id, @Valid @RequestBody CambiarRolRequest request) {
    return usuarioMapper.toResponse(usuarioService.cambiarRol(id, request.rol()));
}

// Service: busca, aplica la regla, guarda y devuelve la entidad
public Usuario cambiarRol(Long id, Rol nuevoRol) {
    Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    usuario.cambiarRol(nuevoRol);   // método de la entidad, lo agrega HU-02
    return usuarioRepository.save(usuario);
}
```

### Errores

Los services **solo lanzan excepciones**; nunca arman respuestas HTTP. `GlobalExceptionHandler` las
convierte en un `ApiError` con el código correcto:

| Lanzar | Respuesta |
|---|---|
| `new ReglaNegocioException("El paquete no cumple con las políticas de tamaño")` | 400 |
| `new RecursoNoEncontradoException("Usuario no encontrado")` | 404 |
| `new ConflictoException("El correo ya está registrado")` | 409 |

Los errores de validación de los DTO (`@NotBlank`, `@Size`...) se convierten solos en un 400 con el error
de cada campo. Por eso cada anotación lleva su mensaje en español:
`@NotBlank(message = "El nombre es obligatorio")`.

### Seguridad

Tiene su propia sección más abajo: [Seguridad: lo que tiene que saber cada historia](#seguridad-lo-que-tiene-que-saber-cada-historia).

### Datos de prueba

En cada arranque la base se crea desde cero (`ddl-auto: create-drop`) y Spring ejecuta
`src/main/resources/data.sql`, que trae los usuarios de prueba, la configuración de tarifas y algunos envíos.

- Para agregar datos de prueba, se agregan `INSERT` a `data.sql`.
- Las columnas van en `snake_case` (`contrasenaHash` → `contrasena_hash`, `emisor` → `emisor_id`).
- No pongas el `id` de usuarios ni envíos (lo genera la base); para referenciar un usuario usa
  `(SELECT id FROM usuarios WHERE correo = '...')`.
- Las contraseñas van como hash BCrypt, nunca en texto plano.
- Si cambias una entidad (agregas o renombras un atributo obligatorio), actualiza también `data.sql`, o el
  back no arrancará.

### Pruebas

Pruebas unitarias con JUnit 5 + Mockito, sin base de datos: se simulan los repositories con `@Mock`.
Ejemplos en `src/test/java/.../config/` (`GlobalExceptionHandlerTest`, `JwtServiceTest`...).

---

## Front (Angular)

### Ideas básicas

Si nunca has usado Angular, estas son las piezas que vas a ver:

| Pieza | Qué es | Ejemplo en el proyecto |
|---|---|---|
| **Componente** | Un pedazo de pantalla: una clase `.ts` (datos y acciones), un `.html` (lo que se ve) y un `.css` (estilos) | `features/inicio/pages/inicio/` |
| **Página** | Un componente que ocupa toda una ruta | `inicio`, `login` |
| **Servicio** | Clase que hace el trabajo que no es de pantalla, como llamar a la API. Se pide con `inject(...)` | `inicio.service.ts` |
| **Signal** | Una variable que avisa cuando cambia: el HTML que la lee se actualiza solo. Se lee con `()` y se cambia con `.set(...)` | `cargando = signal(false)` |
| **Ruta** | Asocia una URL con una página | `app.routes.ts`, `inicio.routes.ts` |
| **Guard** | Decide si se puede entrar a una ruta | `authGuard`, `roleGuard('ADMIN')` |
| **Interceptor** | Código que se ejecuta en cada petición HTTP | agregar el token, manejar los 401 |

En el HTML:
- `{{ nombre() }}` muestra un valor.
- `(click)="guardar()"` ejecuta un método al hacer clic.
- `[disabled]="cargando()"` ata una propiedad a un valor.
- `@if (...) { }` y `@for (x of lista(); track x.id) { }` muestran cosas según una condición o repiten.

### Carpetas

```
src/app/
├── core/        ← lo común a toda la app (compartido: avisar al equipo antes de cambiarlo)
│   ├── auth/          sesión (auth.service.ts), guards
│   ├── interceptors/  token en cada petición y manejo de 401
│   ├── models/        Rol, UsuarioResponse, ApiError
│   └── layout/        marco público, marco privado con menú lateral, barra superior, logo
├── shared/      ← piezas reutilizables: <app-mensaje-error>, <app-cargando>, validadores
└── features/    ← una carpeta por funcionalidad, cada una con pages/, components/, services/, models/
    ├── inicio/        EJEMPLO a copiar
    ├── auth/          HU-01 y HU-03
    ├── usuarios/      HU-02
    └── envios/        HU-04 y HU-05
```

Una feature solo usa `core` y `shared`; nunca importa archivos de otra feature.

### Cómo viaja una petición

```
Página (inicio.ts) → Servicio (inicio.service.ts) → auth.interceptor agrega el token → API
        ↑                                                                               │
        └──── next(respuesta) o error(err) ◀── error.interceptor revisa 401 ◀───────────┘
```

Los componentes **nunca** usan `HttpClient`; siempre llaman al servicio de su feature.

### Patrón de una página

`features/inicio/pages/inicio/inicio.ts` es el ejemplo a copiar:

```ts
protected readonly cargando = signal(false);
protected readonly error = signal<string | null>(null);
protected readonly estadoApi = signal<string | null>(null);

consultarEstado(): void {
  this.cargando.set(true);
  this.error.set(null);
  this.inicioService.obtenerEstadoApi().subscribe({
    next: (respuesta) => { this.estadoApi.set(respuesta.estado); this.cargando.set(false); },
    error: (err) => { this.error.set(err.error?.message); this.cargando.set(false); },
  });
}
```

```html
@if (cargando()) { <app-cargando /> }
<app-mensaje-error [mensaje]="error()" />
```

### Mientras el endpoint no existe

El servicio devuelve datos falsos con el mismo contrato de `docs/api.md`, y la llamada real queda comentada:

```ts
listarUsuarios(): Observable<UsuarioResponse[]> {
  return of([{ id: 1, nombre: 'Ana', /* ... */ }]).pipe(delay(800));
  // return this.http.get<UsuarioResponse[]>(`${environment.apiUrl}/usuarios`);
}
```

Cuando el back esté listo, se cambia solo esa línea.

### Crear una página nueva (paso a paso)

Ejemplo para HU-02 (gestión de usuarios):

1. Crear el componente con el CLI, desde `frontend/`:
   ```bash
   ng g c features/usuarios/pages/gestion-usuarios --skip-tests
   ```
2. Crear `features/usuarios/models/usuario.models.ts` con las interfaces de los DTO (mismos nombres que en el back).
3. Crear `features/usuarios/services/usuarios.service.ts` copiando la forma de `inicio.service.ts`.
4. Crear `features/usuarios/usuarios.routes.ts`:
   ```ts
   export const USUARIOS_ROUTES: Routes = [{ path: '', component: GestionUsuarios }];
   ```
5. Agregar una línea en `app.routes.ts`, dentro de `app`:
   ```ts
   { path: 'usuarios', canActivate: [roleGuard('ADMIN')],
     loadChildren: () => import('./features/usuarios/usuarios.routes').then((m) => m.USUARIOS_ROUTES) },
   ```
6. La opción del menú lateral ya existe en `private-layout.ts` (Usuarios para ADMIN, Nuevo envío para EMISOR).

### Sesión

`AuthService` (`core/auth/auth.service.ts`) guarda el token y el usuario en signals y en `localStorage`:

- `auth.usuario()`, `auth.rol()`, `auth.autenticado()` para leerlos.
- `auth.iniciarSesion(respuestaDelLogin)` después del login (HU-01).
- `auth.cerrarSesion()`.

Los guards solo esconden pantallas: **la seguridad real está en el back**. Ver la sección siguiente.

### Colores y estilos

Los colores de toda la app salen de **dos archivos** (compartidos: avisar antes de cambiarlos):

| Archivo | Qué tiene |
|---|---|
| `src/material-theme.scss` | El tema de Angular Material: color principal verde azulado (`cyan`) y de apoyo azul (`azure`). De aquí salen los colores de botones, tarjetas, campos, tablas, etc. |
| `src/styles.css` | Colores propios de UQDelivery (`--uqd-...`: fondo, menú oscuro, estados de envío) y clases para las páginas. |

Por eso **no hay que pintar los componentes de Material**: un `<button mat-flat-button>` ya sale verde azulado.

**Regla:** en el `.css` de tu pantalla nunca escribas un color a mano (`#ff0000`, `red`). Usa una variable:

```css
.total { color: var(--mat-sys-primary); }             /* color principal */
.ayuda { color: var(--mat-sys-on-surface-variant); }  /* texto gris secundario */
.alerta { color: var(--mat-sys-error); }              /* rojo de error */
```

La lista completa de variables `--mat-sys-...` está en https://material.angular.dev/guide/system-variables.

**Clases globales para las páginas** (en `styles.css`), para que todas se vean igual:

```html
<section class="pagina">              <!-- o "pagina-angosta" para formularios -->
  <header class="encabezado-pagina">
    <h1 class="titulo-pagina">Usuarios</h1>
    <p class="subtitulo-pagina">Cambia el rol de cualquier usuario.</p>
  </header>
  <mat-card appearance="outlined"> ... </mat-card>
</section>
```

**Estado de un envío** (HU-04 y siguientes), con su color ya definido:

```html
<span class="chip-estado" [class]="'estado-' + envio.estado">{{ envio.estado }}</span>
```

Las tarjetas van con `appearance="outlined"`: son blancas y resaltan sobre el fondo gris.

---

## Seguridad: lo que tiene que saber cada historia

La seguridad ya viene armada desde HU-00. Ninguna historia tiene que programar tokens ni filtros, pero
**todas** tienen que hacer bien unas pocas cosas. Si te saltas una, el endpoint funciona igual, pero queda
abierto para quien no debería usarlo. Por eso esta sección es larga: léela antes de empezar tu historia.

### Dos preguntas distintas: ¿quién eres? y ¿puedes hacer esto?

La seguridad responde dos preguntas, y cada una tiene su propio error:

| Pregunta | Nombre técnico | Si falla | Ejemplo |
|---|---|---|---|
| ¿Quién eres? | Autenticación | **401** "Debes iniciar sesión para continuar" | No mandaste token, el token venció o es falso |
| ¿Puedes hacer esto? | Autorización | **403** "No tienes permiso para realizar esta acción" | Un EMISOR intenta listar usuarios (solo ADMIN) |

Regla fácil: **401 = no sé quién eres. 403 = sé quién eres, pero tu rol no puede.**

### Cómo funciona, paso a paso

```
1. Login (HU-01):     correo + contraseña  ──▶  el back responde { token, usuario }
2. El front guarda el token y lo manda en CADA petición:
                      Authorization: Bearer eyJhbGciOi...
3. En el back, antes de llegar a tu controller:
      JwtAuthenticationFilter  ── lee el token, busca al usuario en la base ──▶  "esta petición es de emisor@..."
4. Spring revisa la ruta:
      ¿es pública?            → pasa
      ¿hay usuario?           → si no, 401
5. Tu controller:
      @PreAuthorize("hasRole('EMISOR')")  → si el rol no coincide, 403
6. Tu service:
      usuarioAutenticado.obtener()        → el Usuario que hizo la petición
```

Detalles que conviene saber:

- El **token** es un texto firmado por el back. Guarda el correo, el id y el rol del usuario, y **vence a las
  2 horas**. No hay forma de "refrescarlo": al vencer, el usuario vuelve a iniciar sesión.
- El token no se puede falsificar: si alguien le cambia una letra, la firma ya no coincide y el back lo
  rechaza (401).
- En cada petición el filtro **vuelve a cargar al usuario de la base**. Por eso, si el admin le cambia el rol a
  alguien (HU-02), el cambio aplica en su siguiente petición, aunque su token diga el rol viejo.
- No hay sesiones en el servidor: el back no "recuerda" a nadie. Cada petición se autentica sola con su token.

### Qué rutas piden token

| Rutas | ¿Piden token? |
|---|---|
| `/api/auth/**` (login, registro, recuperar, restablecer) | No, son públicas |
| `/api/health` | No |
| Swagger (`/swagger-ui.html`, `/v3/api-docs/**`) | No |
| **Todo lo demás** | **Sí**, automáticamente |

Esto significa que **no tienes que hacer nada para que tu endpoint pida token**: ya lo pide. Lo que sí tienes
que hacer es decir **qué rol** puede usarlo (siguiente punto).

Si tu endpoint debe ser público y no encaja en `/api/auth/`, hay que agregarlo a la lista `RUTAS_PUBLICAS` de
`SecurityConfig`. Es un archivo compartido: avisa al equipo antes de cambiarlo.

### En el back: lo que tu historia debe hacer

#### 1. Poner el rol en cada método del controller

```java
@GetMapping
@PreAuthorize("hasRole('ADMIN')")                 // solo ADMIN
public List<UsuarioResponse> listar() { ... }

@PostMapping
@PreAuthorize("hasRole('EMISOR')")                // solo EMISOR
public EnvioResponse crear(...) { ... }

@GetMapping("/{id}")
@PreAuthorize("hasAnyRole('ADMIN', 'EMISOR')")    // varios roles
public EnvioResponse obtener(...) { ... }
```

- Se escribe `'ADMIN'`, **sin** el prefijo `ROLE_` (Spring lo agrega solo).
- **Si olvidas `@PreAuthorize`, cualquier usuario con sesión puede usar el endpoint**, sea del rol que sea. El
  endpoint no da error: simplemente queda abierto. Revísalo en cada método.
- Si cualquier usuario con sesión puede usarlo (por ejemplo `GET /api/ciudades`), no pongas `@PreAuthorize`:
  con tener token basta.

#### 2. Sacar el usuario actual del token, nunca del request

Muchas historias necesitan saber **quién** hizo la petición: HU-04 para guardar el emisor del envío, HU-02 para
que el admin no se cambie su propio rol. Para eso ya existe la clase `UsuarioAutenticado`
(`config/security/UsuarioAutenticado.java`). **No hay que crearla ni modificarla: solo pedirla y usarla.**

**De dónde sale el usuario.** Antes de que la petición llegue a tu controller, `JwtAuthenticationFilter` lee el
token, busca al usuario en la base y lo deja anotado en el `SecurityContextHolder` de Spring, que es como una
libreta que dura solo esa petición. `UsuarioAutenticado` lee esa libreta:

| Método | Devuelve | Cuándo usarlo |
|---|---|---|
| `obtener()` | La entidad `Usuario` completa (la busca en la base por el correo del token) | Casi siempre: para guardar el emisor, comparar ids, ver el rol |
| `obtenerCorreo()` | Solo el correo, sin ir a la base | Si solo necesitas el correo |

**Cómo se usa en tu service.** Se declara como un atributo `private final` y Spring lo entrega solo (es una
clase `@Component`; `@RequiredArgsConstructor` de Lombok crea el constructor que lo recibe). Igual que pides un
repository:

```java
@Service
@RequiredArgsConstructor
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final UsuarioAutenticado usuarioAutenticado;   // 1. pedirlo como atributo

    public Envio crear(...) {
        Usuario emisor = usuarioAutenticado.obtener();     // 2. usarlo: quien hizo la petición
        Envio envio = new Envio(codigo, emisor, ...);
        return envioRepository.save(envio);
    }
}
```

Se usa en el **service**, no en el controller: el controller solo pasa los datos de la petición.

Si la ruta es pública (`/api/auth/**`) no hay usuario con sesión y `obtener()` falla. Por eso solo se usa en
endpoints que piden token. En las pruebas unitarias no hay token: se simula con Mockito (ver
[Cómo probar tus endpoints protegidos](#cómo-probar-tus-endpoints-protegidos)).

**Nunca** recibas `emisorId`, `usuarioId` o `rol` en el body o en la URL para saber quién hace la petición.
Cualquiera puede escribir el número que quiera en una petición; el token no se puede falsificar.

| Mal | Bien |
|---|---|
| `EnvioRequest` con un campo `emisorId` | El emisor sale de `usuarioAutenticado.obtener()` |
| `RegistroRequest` con un campo `rol` | El registro siempre crea `EMISOR`; el rol ni siquiera va en el DTO |

#### 3. Contraseñas

- El service recibe la contraseña en texto plano, la encripta con `passwordEncoder.encode(...)` y **solo
  entonces** crea o modifica la entidad. La entidad solo guarda el hash.
- **Nunca** pongas `contrasenaHash` en un `XxxResponse`. `UsuarioResponse` no lo tiene a propósito.
- Nunca escribas en el log contraseñas, hashes ni tokens.
- Para comparar una contraseña con el hash se usa `passwordEncoder.matches(texto, hash)`, nunca `equals`.

#### 4. Validar todo lo que entra

Cada campo de un `XxxRequest` lleva sus anotaciones, y cada `String` lleva un máximo:

```java
public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres")
        String correo
        // ...
) {}
```

El máximo protege contra textos gigantes y contra XSS (código malicioso escondido en un texto). Usa los mismos
máximos que las columnas de las entidades: nombre 100, correo 150, teléfono 20, dirección 200, descripción 500.

#### 5. Consultas a la base

Solo con métodos del repository (`findByCorreo`, `existsByCodigoSeguimiento`...) o `@Query` con parámetros
(`:correo`). **Nunca** armes una consulta pegando texto (`"... where correo = '" + correo + "'"`): eso permite
inyección SQL.

#### 6. Errores

El service lanza `ReglaNegocioException`, `ConflictoException` o `RecursoNoEncontradoException`, y el back
responde solo con el código y el formato correctos. No devuelvas mensajes técnicos (nombres de tablas,
trazas, la excepción original).

### Lo que le toca a cada historia

| HU | Qué cuidar |
|---|---|
| **HU-01 Login** | Usar el `AuthenticationManager` que ya existe: `authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(correo, contrasena))`. Si el correo no existe o la contraseña no coincide, lanza `BadCredentialsException` y el back responde 401 "Correo o contraseña incorrectos": no hay que programar ese caso, y nunca se dice cuál de los dos falló. Pasar el correo a minúsculas antes. Generar el token con `jwtService.generarToken(usuario)` y responder `{ token, usuario }`. |
| **HU-01 Registro** | Siempre crea `EMISOR`. Guardar el correo en minúsculas. Correo repetido → `ConflictoException` (409). Encriptar la contraseña antes de crear el `Usuario`. |
| **HU-02 Roles** | `@PreAuthorize("hasRole('ADMIN')")` en los dos endpoints. El admin no puede cambiar su propio rol: comparar el `id` que llega con `usuarioAutenticado.obtener().getId()` y, si es el mismo, lanzar `ReglaNegocioException`. |
| **HU-03 Recuperación** | Responder **igual** exista o no el correo, para no revelar quién tiene cuenta. Token con `UUID.randomUUID()`, válido 30 minutos y de un solo uso; al pedir uno nuevo se invalidan los anteriores del usuario. Sus endpoints van bajo `/api/auth/`, así que ya son públicos. |
| **HU-04 Envío** | `@PreAuthorize("hasRole('EMISOR')")`. El emisor sale de `usuarioAutenticado.obtener()`. El costo se recalcula siempre en el back; nunca se usa uno que mande el front. |
| **HU-05 Cotización** | `@PreAuthorize("hasRole('EMISOR')")`. Solo calcula, no guarda nada. |

### En el front: lo que tu historia debe hacer

1. **Proteger la ruta con su guard**, aunque sea solo para esconder la pantalla:
   ```ts
   { path: 'usuarios', canActivate: [roleGuard('ADMIN')], loadChildren: ... }
   ```
   Los guards se pueden saltar desde el navegador; por eso la seguridad de verdad es el `@PreAuthorize` del
   back. Los dos van siempre juntos.
2. **No agregues el token a mano.** `auth.interceptor.ts` lo agrega a cada petición, pero **solo si la URL
   empieza con `environment.apiUrl`**. Escribe siempre `` `${environment.apiUrl}/usuarios` ``, nunca
   `'http://localhost:8080/api/usuarios'`.
3. **Un 401 cierra la sesión solo** (`error.interceptor.ts`) y manda al login. La excepción son las rutas
   `/api/auth/`: ahí un 401 significa "correo o contraseña incorrectos" y tu página debe mostrar
   `err.error.message`.
4. **Nunca uses `innerHTML` con datos del usuario.** Mostrar con `{{ }}` es seguro: Angular escapa el texto.
5. **Valida en el formulario con las mismas reglas del back** (obligatorios, máximos, formato de correo,
   contraseña segura). La validación del front es para que el usuario vea el error rápido; la del back es la
   que protege.
6. **HU-01:** al terminar el login real, llama a `authService.iniciarSesion(respuesta)` y borra los botones
   "Entrar como ADMIN / EMISOR / REPARTIDOR".

### Cómo probar tus endpoints protegidos

**Mientras HU-01 no tenga el login:**

- Los botones "Entrar como..." del front crean una sesión **falsa**: el token no es real. Sirven para ver
  pantallas y menús, pero **si tu página llama a un endpoint protegido, el back responde 401 y el front te
  saca al login**. No es un error de tu código.
- Mientras tanto, en el front usa datos falsos en el servicio (`of({...}).pipe(delay(800))`, ver
  [Mientras el endpoint no existe](#mientras-el-endpoint-no-existe)), y prueba el back con las pruebas
  unitarias.

**Cuando HU-01 tenga `POST /api/auth/login`** (va primero precisamente por esto):

1. Arranca el back y abre <http://localhost:8080/swagger-ui.html>.
2. Ejecuta `POST /api/auth/login` con un usuario de prueba de `data.sql`, por ejemplo
   `{ "correo": "admin@uqdelivery.co", "contrasena": "Clave123*" }`.
3. Copia el `token` de la respuesta.
4. Botón **Authorize** (arriba a la derecha) → pega el token **sin** la palabra `Bearer` → Authorize.
5. Ya puedes probar los endpoints protegidos. Para probar otro rol, repite con `emisor@` o `repartidor@`.

**En las pruebas unitarias** no hay token: simula al usuario actual con Mockito.

```java
@Mock UsuarioAutenticado usuarioAutenticado;

@Test
void creaElEnvioANombreDelEmisorAutenticado() {
    Usuario emisor = new Usuario("Ana", "ana@correo.co", "hash", "300", Rol.EMISOR);
    when(usuarioAutenticado.obtener()).thenReturn(emisor);
    // ...
}
```

### Problemas frecuentes

| Me pasa esto | Por qué | Qué hacer |
|---|---|---|
| 401 en Swagger | No pegaste el token, venció (2 h) o lo pegaste con `Bearer ` adelante | Haz login otra vez y pega solo el token |
| 403 | Tu usuario no tiene el rol que pide el `@PreAuthorize` | Entra con el usuario de prueba de ese rol |
| Desde el front, al llamar mi endpoint me saca al login | Estás con un botón de desarrollo (token falso) o tu token venció | Usa datos falsos en el servicio hasta que exista el login real, o vuelve a iniciar sesión |
| Mi endpoint público pide token | No está bajo `/api/auth/` | Muévelo, o agrégalo a `RUTAS_PUBLICAS` avisando al equipo |
| Error de CORS en la consola del navegador | El front no corre en `http://localhost:4200` | Ábrelo en ese puerto, o agrega la dirección a `CORS_ORIGINS` en tu `.env` |
| Cualquier rol puede usar mi endpoint | Falta `@PreAuthorize` en ese método | Agrégalo |

### Piezas que ya existen (no hay que tocarlas)

| Clase (`config/security/`) | Para qué |
|---|---|
| `SecurityConfig` | Rutas públicas, CORS, sin sesión en el servidor, respuestas 401/403 en formato `ApiError`, `PasswordEncoder` (BCrypt) y `AuthenticationManager` |
| `JwtService` | Crear (`generarToken`) y leer tokens |
| `JwtAuthenticationFilter` | Autenticar cada petición a partir de su token |
| `UsuarioDetailsService` | Cargar un usuario por su correo (lo usan el filtro y el login) |
| `UsuarioAutenticado` | Darle al service el usuario que hizo la petición: `obtener()` y `obtenerCorreo()` |

| Archivo del front | Para qué |
|---|---|
| `core/auth/auth.service.ts` | Guardar la sesión (token y usuario) |
| `core/auth/auth.guard.ts` | Solo entra quien tiene sesión |
| `core/auth/role.guard.ts` | Solo entran los roles indicados |
| `core/interceptors/auth.interceptor.ts` | Agregar el token a cada petición |
| `core/interceptors/error.interceptor.ts` | Cerrar la sesión ante un 401 y avisar si el back no responde |

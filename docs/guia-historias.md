# Guía por historia: qué archivos crea y toca cada una

Esta guía dice, para cada historia del Sprint 1, **qué archivos se espera que cree** en el back y en el front,
**qué archivos compartidos tiene que tocar** y **qué puntos delicados** hay que cuidar para no pisarse con los
demás.

Es una guía, no una camisa de fuerza: los nombres y el reparto son una propuesta para que todos trabajen igual
y no haya choques. Si tu historia necesita otro archivo, otro nombre o partir algo de otra forma, hazlo; solo
avisa si afecta a otra persona o a un archivo compartido.

- Las reglas obligatorias (capas, DTOs, seguridad, colores) están en `CLAUDE.md`.
- La explicación de cómo funciona todo está en [arquitectura.md](arquitectura.md).
- El contrato de cada endpoint va en [api.md](api.md), **antes** de programarlo.

---

## 1. Lo que hace cualquier historia

### En el back

Por cada endpoint, el recorrido es siempre el mismo:

```
XxxRequest (dtos/request)  →  XxxController  →  XxxService  →  XxxRepository / métodos de la entidad
                                     ↓
XxxResponse (dtos/response) ←  XxxMapper (config/mapper)  ←  entidad que devuelve el service
```

| Archivo | Qué se espera que tenga |
|---|---|
| `controllers/XxxController.java` | `@RestController`, `@RequestMapping("/api/...")`, `@Tag` de Swagger. Cada método: `@PreAuthorize` con el rol, `@Valid @RequestBody`, llama al service y convierte con el mapper. Nada de lógica. |
| `dtos/request/XxxRequest.java` | `record` con Bean Validation en cada campo y mensajes en español. Todo `String` con `@Size(max = ...)`. |
| `dtos/response/XxxResponse.java` | `record` con lo que sale. Nunca hashes ni datos internos. |
| `config/mapper/XxxMapper.java` | `@Component` escrito a mano: entidad → `XxxResponse`. Opcional: si el request es muy grande, también request → record de `domain/datos/` (ver `CLAUDE.md` 4.2). |
| `services/XxxService.java` | `@Service`. La lógica: busca, aplica reglas, guarda, lanza excepciones (`ReglaNegocioException`, `ConflictoException`, `RecursoNoEncontradoException`). No conoce los DTOs. |
| `repositories/XxxRepository.java` | Los métodos de consulta que necesites (`existsByCorreo`, `findByToken`...). Ya existen los cuatro repositories; solo agregas métodos. |
| `domain/model/Xxx.java` | Los métodos de dominio de tu historia: lo que solo depende de los datos de la entidad (`usuario.cambiarRol(...)`, `token.estaVigente(...)`). Sin setters. |
| `src/test/.../services/XxxServiceTest.java` | Pruebas unitarias del service con Mockito (y de los métodos de entidad que agregues). |

Además:
- **`docs/api.md`**: la sección de cada endpoint (request, response y errores), antes de programarlo.
- **`data.sql`**: si tu historia necesita datos de prueba, agrégalos ahí. Si cambias una entidad, revisa que
  siga funcionando.

### En el front

Cada feature vive en `src/app/features/<feature>/`:

```
features/<feature>/
├── <feature>.routes.ts          rutas de la feature
├── pages/<pagina>/              una carpeta por pantalla (componente que ocupa una ruta)
├── components/<componente>/     piezas de pantalla que no son una ruta (opcional)
├── services/<feature>.service.ts   el único que llama a la API
└── models/<feature>.models.ts      interfaces espejo de los DTOs del back
```

| Archivo | Qué se espera que tenga |
|---|---|
| `models/xxx.models.ts` | `interface` con **los mismos nombres y campos** que los DTOs del back (`EnvioRequest`, `EnvioResponse`). Fechas como `string`, enums como uniones de texto (`'PENDIENTE' \| 'ASIGNADO'`). Si el modelo ya existe en `core/models/` (como `UsuarioResponse`), se importa de ahí. |
| `services/xxx.service.ts` | `@Injectable({ providedIn: 'root' })` con `inject(HttpClient)`. Un método por endpoint, que devuelve `Observable<...>`. URLs con `` `${environment.apiUrl}/...` ``. Mientras el endpoint no exista, datos falsos con `of({...}).pipe(delay(800))`. |
| `pages/xxx/xxx.ts` | Signals `cargando`, `error` y el resultado. Formulario reactivo si hay formulario. Llama al servicio con `.subscribe({ next, error })` y muestra `err.error.message`. Copia la forma de `features/inicio/`. |
| `pages/xxx/xxx.html` | Componentes de Angular Material. Envuelto en `<section class="pagina">` (o `pagina-angosta` para formularios) con `encabezado-pagina`, `titulo-pagina` y `subtitulo-pagina`. Usa `<app-cargando>` y `<app-mensaje-error>`. |
| `pages/xxx/xxx.css` | Solo lo propio de la pantalla (distribución, espacios). **Colores siempre con variables** (`var(--mat-sys-primary)`), nunca a mano. |
| `xxx.routes.ts` | `export const XXX_ROUTES: Routes = [...]` con las rutas de la feature. |

Para crear componentes y servicios se usa el CLI, desde `frontend/`:

```bash
ng g c features/usuarios/pages/gestion-usuarios --skip-tests
ng g s features/usuarios/services/usuarios --skip-tests
```

---

## 2. Archivos compartidos: quién los toca y cómo

Son los archivos que usan varias historias. Tocarlos está bien cuando la historia lo necesita; lo importante
es **cambiar solo tu parte** y avisar.

| Archivo | Quién lo toca | Qué cambio se espera |
|---|---|---|
| `frontend/src/app/app.routes.ts` | HU-02, HU-04 | Descomentar **su** línea (ya está escrita) e importar `roleGuard`. Nada más. |
| `features/auth/auth.routes.ts` | HU-01, HU-03 | Agregar **sus** rutas a la lista. |
| `features/auth/pages/login/` | HU-01 (dueño), HU-03 | HU-01 la reemplaza por el formulario real. HU-03 solo agrega el enlace "¿Olvidaste tu contraseña?" (coordinar con HU-01). |
| `core/layout/private-layout/private-layout.ts` | Nadie en el Sprint 1 | Las opciones del menú (Usuarios, Nuevo envío) ya existen. Si una pantalla nueva necesita opción en el menú, se agrega una línea a `OPCIONES` y se avisa. |
| `shared/validators/` | HU-01 | Crea `contrasena-segura.validator.ts`, que también usa HU-03. |
| `backend/.../config/security/SecurityConfig.java` | Nadie | Las rutas `/api/auth/**` ya son públicas y todo lo demás ya pide token. Solo se toca si un endpoint público no va bajo `/api/auth/`. |
| `backend/src/main/resources/data.sql` | Quien necesite datos | Agregar `INSERT` al final de la sección que corresponda. |
| `docs/api.md` | Todas | Agregar la sección de cada endpoint. |
| `core/`, `app.config.ts`, `styles.css`, `material-theme.scss`, `build.gradle`, `package.json` | Nadie sin avisar | Si de verdad hace falta, PR pequeño y aparte. |

Las líneas de `app.routes.ts` ya preparadas se ven así; al descomentarlas se completa el `loadChildren`:

```ts
import { roleGuard } from './core/auth/role.guard';
// ...
{ path: 'usuarios', canActivate: [roleGuard('ADMIN')],
        loadChildren: () => import('./features/usuarios/usuarios.routes').then((m) => m.USUARIOS_ROUTES) },
```

### Dependencias entre historias

Hay tres puntos donde una historia necesita algo de otra. Conviene acordarlos **el primer día**:

| Quién necesita | Qué | De quién | Cómo resolverlo |
|---|---|---|---|
| HU-02 | `UsuarioResponse` y `UsuarioMapper` | HU-01 | HU-01 los sube en su primer PR, junto con el login. Mientras tanto, HU-02 puede crearlos igual y luego se unifican. |
| Todas | `POST /api/auth/login` (para probar endpoints protegidos) | HU-01 | HU-01 entrega el login primero. Mientras tanto: pruebas unitarias y datos falsos en el front. |
| HU-04 | `CotizacionService.cotizar(...)` | HU-05 | Acordar la firma del método el primer día. HU-04 la simula con Mockito en sus pruebas hasta que HU-05 la suba. |
| HU-04 ↔ HU-05 (front) | `<app-resumen-cotizacion>` | HU-05 | Acordar sus `input()` y `output()` (qué datos le pasa el formulario y qué devuelve). |

---

## 3. Historia por historia

### HU-01 · Registro e inicio de sesión

**Back**

| Archivo | Contenido |
|---|---|
| `controllers/AuthController.java` | `@RequestMapping("/api/auth")`: `POST /login` y `POST /registro`. Públicos: sin `@PreAuthorize`. |
| `dtos/request/LoginRequest`, `RegistroRequest` | Login: correo, contraseña. Registro: nombre, correo, teléfono, contraseña, confirmación. **Sin campo `rol`.** |
| `dtos/response/LoginResponse`, `UsuarioResponse` | `{ token, usuario }` y los datos públicos del usuario (sin hash). `UsuarioResponse` lo reutiliza HU-02. |
| `config/mapper/UsuarioMapper.java` | `Usuario` → `UsuarioResponse`. Lo reutiliza HU-02. |
| `services/AuthService.java` | `login(correo, contrasena)` y `registrar(...)`. |
| `UsuarioRepository` | `existsByCorreo(...)`. |

Puntos delicados:
- Login con `authenticationManager.authenticate(...)` (ya existe en `SecurityConfig`) y token con
  `jwtService.generarToken(usuario)`. No hay que programar el caso "contraseña incorrecta": Spring lanza
  `BadCredentialsException` y el back responde 401.
- Correo en minúsculas antes de buscar o guardar. Duplicado → `ConflictoException` (409).
- Contraseña con `passwordEncoder.encode(...)` **antes** de crear el `Usuario`.
- El registro siempre crea `EMISOR`.

**Front** (`features/auth/`)

| Archivo | Contenido |
|---|---|
| `pages/login/` | Reemplaza la página falsa por el formulario real (correo y contraseña). Borra los botones "Entrar como...". |
| `pages/registro/` | Formulario con las mismas reglas del back. |
| `services/auth-api.service.ts` | `login(...)` y `registrar(...)`. Se llama `auth-api` para no confundirse con `core/auth/auth.service.ts`. |
| `models/auth.models.ts` | `LoginRequest`, `RegistroRequest`. `LoginResponse` y `UsuarioResponse` ya están en `core/models/`. |
| `shared/validators/contrasena-segura.validator.ts` | Mínimo 8, una mayúscula, una minúscula y un número. |
| `auth.routes.ts` | Agregar `{ path: 'registro', component: Registro }`. |

Puntos delicados:
- Después del login: `authService.iniciarSesion(respuesta)` (de `core`) y navegar a `/app/inicio`.
- Un 401 en el login **no** cierra sesión ni redirige (el interceptor lo deja pasar): la página muestra
  `err.error.message`.

### HU-02 · Gestión de perfiles y roles

**Back**

| Archivo | Contenido |
|---|---|
| `controllers/UsuarioController.java` | `@RequestMapping("/api/usuarios")`: `GET` (listar) y `PATCH /{id}/rol`. **`@PreAuthorize("hasRole('ADMIN')")` en los dos.** |
| `dtos/request/CambiarRolRequest.java` | `rol` con `@NotNull`. |
| `services/UsuarioService.java` | `listar()` y `cambiarRol(id, nuevoRol)`. |
| `Usuario.java` | Método `cambiarRol(Rol nuevoRol)`. |

Puntos delicados:
- El admin no puede cambiar su propio rol: comparar el `id` con `usuarioAutenticado.obtener().getId()` y lanzar
  `ReglaNegocioException`.
- Reutiliza `UsuarioResponse` y `UsuarioMapper` de HU-01.

**Front** (`features/usuarios/`)

| Archivo | Contenido |
|---|---|
| `usuarios.routes.ts` | `USUARIOS_ROUTES` con la página. |
| `pages/gestion-usuarios/` | `mat-table` con los usuarios y un selector de rol por fila. |
| `services/usuarios.service.ts` | `listar()` y `cambiarRol(id, rol)`. |
| `models/usuario.models.ts` | `CambiarRolRequest`. `UsuarioResponse` se importa de `core/models/`. |
| `app.routes.ts` | Descomentar la línea de `usuarios` con `roleGuard('ADMIN')`. |

### HU-03 · Recuperación de contraseña

**Back**

| Archivo | Contenido |
|---|---|
| `controllers/RecuperacionController.java` | También con `@RequestMapping("/api/auth")`: `POST /recuperar` y `POST /restablecer`. Va en un controller propio para no chocar con el `AuthController` de HU-01. Públicos. |
| `dtos/request/RecuperarRequest`, `RestablecerRequest` | Correo · token, contraseña nueva y confirmación. |
| `services/RecuperacionService.java` | `solicitar(correo)` y `restablecer(token, contrasena)`. |
| `services/CorreoService.java` | Envía correos con `JavaMailSender` (la configuración de Brevo ya está en `application.yml`). Lo reutilizarán las notificaciones del Sprint 2. |
| `TokenRecuperacionRepository` | `findByToken(...)` y lo necesario para invalidar los tokens anteriores del usuario. |
| `TokenRecuperacion.java` / `Usuario.java` | Métodos como `estaVigente(ahora)`, `marcarUsado()` y `cambiarContrasena(hash)`. |

Puntos delicados:
- Misma respuesta exista o no el correo.
- Token `UUID.randomUUID()`, 30 minutos, un solo uso; pedir uno nuevo invalida los anteriores.
- El enlace del correo apunta al front: `${app.frontend-url}/restablecer?token=...`.
- En las pruebas, `CorreoService` se simula con Mockito (no se envían correos de verdad).

**Front** (`features/auth/`)

| Archivo | Contenido |
|---|---|
| `pages/solicitar-recuperacion/` | Campo de correo y mensaje de confirmación. |
| `pages/restablecer-contrasena/` | Lee el `token` de la URL (`?token=...`) y pide la contraseña nueva dos veces. Usa `contrasena-segura` y `contrasenasIguales`. |
| `services/recuperacion.service.ts` | `solicitar(correo)` y `restablecer(...)`. |
| `models/recuperacion.models.ts` | `RecuperarRequest`, `RestablecerRequest`. |
| `auth.routes.ts` | Agregar `recuperar` y `restablecer`. |

Puntos delicados:
- Como `app.config.ts` usa `withComponentInputBinding()`, el `token` de la URL puede llegar como
  `readonly token = input<string>()` en la página.
- El enlace "¿Olvidaste tu contraseña?" va en la página de login, que es de HU-01: coordinar.

### HU-04 · Creación de solicitud de envío

**Back**

| Archivo | Contenido |
|---|---|
| `controllers/EnvioController.java` | `@RequestMapping("/api/envios")`: `POST` (crear) con `@PreAuthorize("hasRole('EMISOR')")`. |
| `controllers/CiudadController.java` | `GET /api/ciudades`: cualquier usuario con sesión (sin `@PreAuthorize`). |
| `controllers/ConfiguracionController.java` | `GET /api/configuracion/politicas`: límites de peso y medidas para validar el formulario. |
| `dtos/request/EnvioRequest.java` | Todos los campos del formulario. **Sin `emisorId` ni `costo`.** |
| `dtos/response/EnvioResponse`, `CiudadResponse`, `PoliticasResponse` | Lo que devuelve cada endpoint. |
| `config/mapper/EnvioMapper.java` | `Envio` → `EnvioResponse`. |
| `services/EnvioService.java` | `crear(...)`: toma el emisor de `usuarioAutenticado.obtener()`, llama a la cotización de HU-05, genera el código y guarda. |

Recomendación (opcional): como `EnvioRequest` tiene muchos campos, en vez de pasarle al service más de diez
parámetros sueltos se puede crear un record `domain/datos/DatosEnvio.java` y que el mapper convierta el request
en él. Es una decisión interna de HU-04: no afecta a las demás historias.
| `EnvioRepository` | `existsByCodigoSeguimiento(...)`. |

Puntos delicados:
- El costo **siempre** lo calcula el back con `CotizacionService` (HU-05).
- Código `UQD-` + 8 caracteres sin O, 0, I ni 1; si ya existe, se genera otro.

**Front** (`features/envios/`)

| Archivo | Contenido |
|---|---|
| `envios.routes.ts` | `nuevo` y la pantalla de confirmación (p. ej. `creado/:codigo`). |
| `pages/nuevo-envio/` | Formulario (ciudades de `GET /api/ciudades`, límites de `GET /api/configuracion/politicas`). Inserta `<app-resumen-cotizacion>`. |
| `pages/envio-creado/` | Muestra el código de seguimiento. |
| `services/envios.service.ts` | `crear(...)`, `listarCiudades()`, `obtenerPoliticas()`. |
| `models/envio.models.ts` | `EnvioRequest`, `EnvioResponse`, `CiudadResponse`, `PoliticasResponse`, tipo `EstadoEnvio`. |
| `app.routes.ts` | Descomentar la línea de `envios` con `roleGuard('EMISOR')`. |

Puntos delicados:
- Si el usuario cambia peso, medidas o ciudades después de cotizar, la cotización se borra y debe cotizar otra vez.
- Para mostrar estados de envío, usar `<span class="chip-estado" [class]="'estado-' + estado">`.

### HU-05 · Cotización automática

**Back**

| Archivo | Contenido |
|---|---|
| `controllers/CotizacionController.java` | También con `@RequestMapping("/api/envios")`: `POST /cotizacion` con `@PreAuthorize("hasRole('EMISOR')")`. Controller propio para no chocar con el `EnvioController` de HU-04. |
| `dtos/request/CotizacionRequest.java` | Ciudades de origen y destino, peso y medidas. |
| `dtos/response/CotizacionResponse.java` | `costo` y `distanciaKm`. |
| `domain/datos/Cotizacion.java` | **Ya existe** (HU-00): record `Cotizacion(costo, distanciaKm)`, lo que devuelve `cotizar(...)`. No es una tabla. |
| `config/mapper/CotizacionMapper.java` | `Cotizacion` → `CotizacionResponse`. |
| `services/CotizacionService.java` | `cotizar(...)`: lee `configuracion_tarifa`, valida límites, calcula distancia y costo. Lo usa también HU-04. |
| `Ciudad.java` / `ConfiguracionTarifa.java` | Métodos de dominio: distancia Haversine entre ciudades, `cumpleLimites(...)`, `calcularCosto(...)`. |

Puntos delicados:
- Solo calcula; no guarda nada.
- Redondeo a la centena con `RoundingMode.HALF_UP`. La cotización es determinista: probar con valores exactos.
- Las tarifas se leen de la base (`ConfiguracionTarifa.ID_UNICO`), nunca se escriben en el código.

**Front** (`features/envios/`)

| Archivo | Contenido |
|---|---|
| `components/resumen-cotizacion/` | Tarjeta con el botón «Cotizar» y el resultado (costo en pesos y distancia). **No llama a la API**: recibe datos con `input()` y avisa con `output()`. Incluye su prueba `resumen-cotizacion.spec.ts`. |
| `services/cotizacion.service.ts` | `cotizar(request)`: único que llama a `POST /api/envios/cotizacion`. Incluye `cotizacion.service.spec.ts`. |
| `models/cotizacion.models.ts` | `CotizacionRequest`, `CotizacionResponse` y el tipo `CiudadCodigo` (propio de esta historia para no editar los modelos de HU-04). |

No crea rutas ni páginas: su componente vive dentro del formulario de HU-04.

**Cómo usa HU-04 el componente.** La página `nuevo-envio` es la que llama al servicio y guarda el resultado
en signals; el componente solo lo muestra:

| Nombre | Tipo | Qué hace |
|---|---|---|
| `cotizacion` | `input<CotizacionResponse \| null>` | Resultado de cotizar. `null` si aún no se cotizó. |
| `cargando` | `input<boolean>` | `true` mientras se espera al back: muestra el indicador de carga y bloquea el botón. |
| `error` | `input<string \| null>` | Mensaje del `ApiError` (p. ej. «El paquete no cumple con las políticas de tamaño»). |
| `deshabilitado` | `input<boolean>` | Poner `true` mientras el formulario del paquete sea inválido. |
| `cotizar` | `output<void>` | Se emite cuando el usuario pulsa «Cotizar». |

En la página de HU-04:

```ts
protected readonly cotizacion = signal<CotizacionResponse | null>(null);
protected readonly cotizando = signal(false);
protected readonly errorCotizacion = signal<string | null>(null);

cotizar(): void {
  this.cotizando.set(true);
  this.errorCotizacion.set(null);
  this.cotizacionService.cotizar(this.formulario.getRawValue()).subscribe({
    next: (respuesta) => {
      this.cotizacion.set(respuesta);
      this.cotizando.set(false);
    },
    error: (err) => {
      this.errorCotizacion.set(err.error?.message ?? 'No se pudo cotizar el envío');
      this.cotizando.set(false);
    },
  });
}
```

```html
<app-resumen-cotizacion
        [cotizacion]="cotizacion()"
        [cargando]="cotizando()"
        [error]="errorCotizacion()"
        [deshabilitado]="formulario.invalid"
        (cotizar)="cotizar()"
/>
```

Puntos delicados del front:
- **Borrar la cotización al cambiar el formulario.** Si el usuario cambia peso, medidas o ciudades después de
  cotizar, la página de HU-04 pone `cotizacion` en `null` para obligarlo a cotizar de nuevo (`CLAUDE.md` 4.6).
- **Lo que muestra la tarjeta es informativo.** El back siempre recalcula el costo al crear el envío
  (`POST /api/envios`); nunca se envía el costo del front.
- **Formato de moneda con `Intl.NumberFormat('es-CO')`**, así no hace falta registrar el idioma en
  `app.config.ts` (archivo compartido).
- **Probar con el back real:** el token del login de desarrollo es falso y el back responde 401. La prueba de
  punta a punta necesita el login real de HU-01.

---

## 4. Antes de abrir el PR

La lista completa está en la plantilla del PR (`.github/pull_request_template.md`). Lo que más se olvida:

- `@PreAuthorize` en **cada** método del controller que lo necesite.
- Mensajes de validación en español en cada anotación del DTO.
- La sección de tus endpoints en `docs/api.md`.
- `git pull origin main` antes de abrir el PR, y comprobar que el back arranca y el front compila.
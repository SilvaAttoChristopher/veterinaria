# Plan de Desarrollo — Frontend + Backend Completo

**Modelo de referencia:** `Paginawebv1/`
**Arquitectura:** Thymeleaf + Bootstrap 5.3.8 + Spring Boot (servidor y frontend en el mismo proyecto)
**Auth:** Session-based con Spring Security
**Base de datos:** PostgreSQL con Docker Compose

---

## Arquitectura Thymeleaf

El frontend utiliza **Thymeleaf** como motor de plantillas server-side. Las páginas se renderizan en el servidor y se sirven como HTML completo.

### Estructura de directorios

```
src/main/resources/
├── templates/                    # Plantillas Thymeleaf
│   ├── index.html               # Landing page (pública)
│   ├── staff.html               # Login staff (público)
│   ├── fragments/
│   │   ├── layout.html          # Fragmentos comunes (header, footer, modales)
│   │   └── dashboard-layout.html # Fragmentos del dashboard (sidebar)
│   └── dashboard/
│       ├── perfil.html          # Perfil cliente
│       ├── perfil-medico.html   # Perfil veterinario
│       ├── mascotas.html        # Gestión de mascotas
│       ├── citas.html           # Gestión de citas
│       └── notificaciones.html  # Centro de notificaciones
├── static/                       # Recursos estáticos
│   ├── css/
│   │   ├── estilos.css          # Estilos landing page
│   │   └── dashboard.css        # Estilos dashboard
│   ├── js/
│   │   ├── auth.js              # Funciones de autenticación
│   │   └── dashboard.js         # Lógica del dashboard
│   └── imagenes/                # Imágenes del proyecto
└── db/
    └── init.sql                 # Script de inicialización PostgreSQL
```

### Controladores Thymeleaf

Los controladores de páginas retornan nombres de vistas (Thymeleaf resuelve las plantillas):

```java
@Controller
public class PageController {
    @GetMapping("/")
    public String index() { return "index"; }
    
    @GetMapping("/dashboard/perfil")
    public String dashboardPerfil() { return "dashboard/perfil"; }
}
```

### Controladores REST

Los controladores REST retornan JSON para AJAX:

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/login")
    public ResponseEntity<?> login() { ... }
}
```

### Flujo de autenticación

1. Usuario completa formulario en Thymeleaf
2. JavaScript envía `POST /api/auth/login` via `fetch()`
3. Spring Security procesa y retorna JSON con rol
4. JavaScript redirige a Thymeleaf dashboard según rol
5. Dashboard carga datos via AJAX (`/api/...`)

---

## Fase 1 — Configuración del proyecto y entorno

**Objetivo:** Tener el proyecto compilando, ejecutándose con Docker, y el frontend servido desde Spring Boot con Thymeleaf.

### Tareas

1. Agregar dependencia `spring-boot-starter-thymeleaf` y `thymeleaf-extras-springsecurity6` al `pom.xml`
2. Mover CSS, JS e imágenes a `src/main/resources/static/`
3. Crear plantillas Thymeleaf en `src/main/resources/templates/`
4. Crear fragmentos reutilizables (`fragments/layout.html`, `fragments/dashboard-layout.html`)
5. Configurar `application.properties` para Thymeleaf
6. Verificar que `localhost:8080/` muestra la landing page

### Criterio de aceptación
- `docker compose up --build` levanta todo
- `localhost:8080` muestra la landing page renderizada por Thymeleaf
- Las imágenes y CSS cargan correctamente desde `/static/`

---

## Fase 2 — Backend: Servicios (Service Layer)

**Objetivo:** Crear la capa de lógica de negocio para todas las entidades.

### Tareas

Crear clases de servicio en `src/main/java/com/example/veterinaria/service/`:

| Servicio | Entidad | Operaciones |
|---|---|---|
| `UsuarioService` | Usuario | CRUD, buscar por username |
| `ClienteService` | Cliente | CRUD, buscar por DNI/nombre |
| `VeterinarioService` | Veterinario | CRUD, buscar por especialidad |
| `MascotaService` | Mascota | CRUD, listar por cliente |
| `CartillaService` | Cartilla | CRUD, listar por mascota |
| `VacunaService` | Vacuna | CRUD, listar activas |
| `VacunacionService` | Vacunacion | CRUD, listar por cartilla |
| `HorarioService` | Horario | CRUD, listar por veterinario |
| `HistorialService` | Historial | CRUD, listar por mascota/veterinario |
| `ServicioService` | Servicio | CRUD, listar activos |
| `CitaService` | Cita | CRUD, verificar disponibilidad |
| `BoletaService` | Boleta | CRUD, listar por cliente |
| `DetalleBoletaService` | DetalleBoleta | CRUD, listar por boleta |
| `RolService` | Rol | CRUD |
| `NotificacionService` | Notificacion | CRUD, listar por usuario, marcar leída |

### Criterio de aceptación
- Todos los servicios compilan
- Cada servicio tiene al menos: `findAll()`, `findById()`, `save()`, `deleteById()`
- `./mvnw compile` BUILD SUCCESS

---

## Fase 3 — Backend: Controladores REST + Thymeleaf

**Objetivo:** Exponer la API REST y las rutas Thymeleaf para el frontend.

### Controladores Thymeleaf (retornan vistas)

```java
@Controller
public class PageController {
    @GetMapping("/")                    → "index"
    @GetMapping("/staff")               → "staff"
    @GetMapping("/dashboard/perfil")    → "dashboard/perfil"
    @GetMapping("/dashboard/mascotas")  → "dashboard/mascotas"
    @GetMapping("/dashboard/citas")     → "dashboard/citas"
    @GetMapping("/dashboard/notificaciones") → "dashboard/notificaciones"
    @GetMapping("/dashboard/perfil-medico") → "dashboard/perfil-medico"
}
```

### Controladores REST (retornan JSON)

```http
# Auth
POST /api/auth/login
POST /api/auth/logout
POST /api/auth/registro
GET  /api/auth/me

# Usuarios
GET    /api/usuarios
GET    /api/usuarios/{id}
POST   /api/usuarios
PUT    /api/usuarios/{id}
DELETE /api/usuarios/{id}

# Clientes
GET    /api/clientes
GET    /api/clientes/{id}
POST   /api/clientes
PUT    /api/clientes/{id}
DELETE /api/clientes/{id}
GET    /api/clientes/buscar?criterio=

# Veterinarios
GET    /api/veterinarios
GET    /api/veterinarios/{id}
POST   /api/veterinarios
PUT    /api/veterinarios/{id}
DELETE /api/veterinarios/{id}

# Mascotas
GET    /api/mascotas
GET    /api/mascotas/{id}
POST   /api/mascotas
PUT    /api/mascotas/{id}
DELETE /api/mascotas/{id}
GET    /api/mascotas/cliente/{idUsuario}

# Cartilla
GET    /api/cartillas
POST   /api/cartillas
GET    /api/cartillas/mascota/{idMascota}

# Vacunas
GET    /api/vacunas
POST   /api/vacunas
PUT    /api/vacunas/{id}
DELETE /api/vacunas/{id}

# Vacunaciones
GET    /api/vacunaciones
POST   /api/vacunaciones
GET    /api/vacunaciones/cartilla/{idCartilla}

# Horarios
GET    /api/horarios
POST   /api/horarios
GET    /api/horarios/veterinario/{idUsuario}
PUT    /api/horarios/{id}
DELETE /api/horarios/{id}

# Historial
GET    /api/historial
POST   /api/historial
GET    /api/historial/mascota/{idMascota}
GET    /api/historial/veterinario/{idUsuario}

# Servicios
GET    /api/servicios
POST   /api/servicios
PUT    /api/servicios/{id}
DELETE /api/servicios/{id}

# Citas
GET    /api/citas
GET    /api/citas/{id}
POST   /api/citas
PUT    /api/citas/{id}
DELETE /api/citas/{id}
GET    /api/citas/mascota/{idMascota}
GET    /api/citas/veterinario/{idUsuario}/fecha

# Boletas
GET    /api/boletas
GET    /api/boletas/{id}
POST   /api/boletas
GET    /api/boletas/cliente/{idUsuario}

# Detalle Boleta
GET    /api/detalle-boletas
POST   /api/detalle-boletas
GET    /api/detalle-boletas/boleta/{idBoleta}

# Notificaciones
GET    /api/notificaciones
GET    /api/notificaciones/usuario/{idUsuario}
GET    /api/notificaciones/usuario/{idUsuario}/pendientes
PATCH  /api/notificaciones/{id}/leida

# Roles
GET    /api/roles
POST   /api/roles
```

### Criterio de aceptación
- Todos los endpoints Thymeleaf retornan HTML válido
- Todos los endpoints REST responden con JSON
- `./mvnw compile` BUILD SUCCESS

---

## Fase 4 — Backend: Seguridad (Spring Security)

**Objetivo:** Autenticación por sesión, control de acceso por rol.

### Tareas

1. Configurar `WebSecurityConfig`:
   - Login: `POST /api/auth/login` (procesado por Spring Security)
   - Logout: `POST /api/auth/logout`
   - Registro: `POST /api/auth/registro`
   - Rutas públicas: `/`, `/staff`, `/css/**`, `/js/**`, `/imagenes/**`
   - Rutas protegidas: `/dashboard/**`, `/api/**`
   - Roles: `ADMIN`, `CLIENTE`, `VETERINARIO`

2. Crear `CustomUserDetailsService` que busque en la tabla `usuario`

3. Crear `AuthController`:
   ```http
   POST /api/auth/login      → body: { username, password }
   POST /api/auth/logout
   POST /api/auth/registro    → body: { username, password, nombres, apellidos, dni, ... }
   GET  /api/auth/me          → retorna usuario logueado
   ```

4. Hashear contraseñas con `BCryptPasswordEncoder`

5. Configurar redirección según rol después del login

### Criterio de aceptación
- Login funcional con sesión
- Logout invalida la sesión
- Registro crea usuario + cliente
- Rutas protegidas redirigen a login sin sesión
- `/api/auth/me` retorna el usuario actual

---

## Fase 5 — Frontend: Landing Page y Autenticación

**Objetivo:** Completar la landing page y el flujo de login/registro conectados al backend.

### Tareas

1. **Landing page** (`templates/index.html`):
   - Thymeleaf renderiza la página con fragmentos reutilizables
   - Conectar modal de Login con `POST /api/auth/login` via JavaScript
   - Conectar modal de Registro con `POST /api/auth/registro` via JavaScript
   - Redirigir a dashboard según rol después del login

2. **Staff login** (`templates/staff.html`):
   - Conectar formulario con `POST /api/auth/login`
   - Redirigir a `/dashboard/perfil-medico` para VETERINARIO

3. **Lógica de sesión** (`js/auth.js`):
   ```javascript
   // Funciones
   login(username, password)
   logout()
   getCurrentUser()
   redirectBasedOnRole(rol)
   ```

4. **Protección de páginas**:
   - Cada dashboard verifica sesión al cargar via `GET /api/auth/me`
   - Sin sesión → redirigir a `/`

### Criterio de aceptación
- Login funcional desde la landing page
- Registro crea cuenta y redirige al dashboard
- Staff login funciona
- Cerrar sesión vuelve a la landing

---

## Fase 6 — Frontend: Dashboard Cliente

**Objetivo:** Dashboard completo del cliente con CRUD de perfil, mascotas, citas y notificaciones.

### Tareas

1. **Perfil** (`templates/dashboard/perfil.html`):
   - Thymeleaf renderiza datos del usuario desde el controller
   - Botón "Editar" que permite modificar nombre, teléfono, dirección
   - Guardar cambios con `PUT /api/usuarios/{id}` via AJAX

2. **Mascotas** (`templates/dashboard/mascotas.html`):
   - Lista de mascotas del cliente desde `GET /api/mascotas/cliente/{id}`
   - Tarjetas con: nombre, especie, raza, sexo, fecha nacimiento
   - Botón "Registrar mascota" → modal con formulario
   - Botón "Ver detalles" → modal con historial y cartilla

3. **Citas** (`templates/dashboard/citas.html`):
   - Lista de citas del cliente desde `GET /api/citas`
   - Filtros: fecha, estado
   - Botón "Agendar cita" → modal con formulario
   - Estados: PROGRAMADA, ATENDIDA, CANCELADA, NO_ASISTIO

4. **Notificaciones** (`templates/dashboard/notificaciones.html`):
   - Lista desde `GET /api/notificaciones/usuario/{id}`
   - Marcar como leída con `PATCH /api/notificaciones/{id}/leida`
   - Badge con cantidad de pendientes en el menú

### Criterio de aceptación
- Cliente puede ver y editar su perfil
- Cliente puede registrar y ver sus mascotas
- Cliente puede agendar y ver sus citas
- Cliente puede ver y marcar notificaciones

---

## Fase 7 — Frontend: Dashboard Veterinario

**Objetivo:** Dashboard del veterinario con gestión de horarios, citas, historial y mascotas.

### Tareas

1. **Perfil** (`templates/dashboard/perfil-medico.html`):
   - Mostrar datos + especialidad + información de clínica
   - Botón "Editar" para modificar teléfono, dirección

2. **Mascotas** (`templates/dashboard/mascotas.html`):
   - Lista de mascotas atendidas (desde citas/historial)
   - Ver detalles: cartilla, vacunaciones, historial

3. **Citas** (`templates/dashboard/citas.html`):
   - Lista de citas asignadas al veterinario
   - Filtros: fecha, estado
   - Cambiar estado: PROGRAMADA → ATENDIDA / CANCELADA / NO_ASISTIO
   - Registrar atención (crear historial)

4. **Horarios** (sección dentro del perfil o página dedicada):
   - Lista de horarios desde `GET /api/horarios/veterinario/{id}`
   - Agregar/editar/eliminar horarios
   - Formulario: día_semana, hora_inicio, hora_fin

5. **Notificaciones** (`templates/dashboard/notificaciones.html`):
   - Mismo estilo que el dashboard del cliente

### Criterio de aceptación
- Veterinario puede ver su perfil y horarios
- Veterinario puede gestionar sus citas
- Veterinario puede registrar historial de atención
- Veterinario puede ver mascotas atendidas

---

## Fase 8 — Backend: Lógica de negocio avanzada

**Objetivo:** Implementar reglas de negocio que requieren lógica en el servicio.

### Tareas

1. **Verificar disponibilidad de horario** (CitaService):
   - No permitir dos citas para el mismo veterinario, fecha y hora
   - Verificar que el veterinario tiene horario para ese día/hora

2. **Generar boleta automáticamente** (BoletaService):
   - Al crear una cita ATENDIDA, generar boleta con el servicio
   - Calcular subtotal, IGV (18%), total

3. **Notificaciones automáticas** (NotificacionService):
   - Al agendar cita → notificación CITA_PROXIMA
   - Al registrar vacunación con proxima_dosis → notificación VACUNACION_PROXIMA

4. **Validaciones de integridad**:
   - Mascota debe pertenecer a un cliente
   - Cartilla solo puede tener una por mascota
   - Vacunación debe referenciar vacuna vigente

### Criterio de aceptación
- Cita duplicada retorna 409 CONFLICT
- Boleta se genera con montos correctos
- Notificaciones se crean automáticamente

---

## Fase 9 — Frontend: Funcionalidades avanzadas

**Objetivo:** Completar todas las funcionalidades del frontend.

### Tareas

1. **Búsqueda y filtros**:
   - Buscar clientes por nombre/DNI/teléfono
   - Filtrar mascotas por especie/raza
   - Filtrar citas por fecha/estado/veterinario

2. **Historial de atención** (veterinario):
   - Formulario para registrar atención
   - Lista de atenciones anteriores

3. **Cartilla de vacunación**:
   - Ver cartilla de una mascota
   - Registrar vacunación
   - Próximas dosis pendientes

4. **Boletas**:
   - Ver boletas del cliente
   - Detalle de boleta con servicios
   - Imprimir/PDF (opcional)

5. **Gestión de servicios** (admin):
   - CRUD de servicios
   - Activar/desactivar

6. **Gestión de roles y permisos** (admin):
   - Asignar roles a usuarios
   - Gestionar permisos

### Criterio de aceptación
- Búsqueda funciona en todas las secciones
- Cartilla se puede consultar y actualizar
- Boletas se generan y muestran correctamente
- Admin puede gestionar servicios y roles

---

## Fase 10 — Pulido, responsive y pruebas

**Objetivo:** Asegurar calidad, compatibilidad móvil y completitud.

### Tareas

1. **Responsive**:
   - Verificar todos los dashboards en móvil (sidebar colapsa)
   - Ajustar grids y tarjetas para pantallas pequeñas

2. **Manejo de errores**:
   - Toasts/notificaciones de éxito/error
   - Loading states
   - Mensajes de error amigables

3. **Validación de formularios**:
   - Frontend: required, patterns, maxlength
   - Backend: Bean Validation (@NotBlank, @Email, etc.)

4. **Pruebas manuales**:
   - Flujo completo: registro → login → agendar cita → veterinario atiende → boleta generada
   - Verificar todas las rutas de navegación
   - Verificar cierre de sesión

5. **Documentación**:
   - Actualizar README con instrucciones
   - Documentar endpoints API

### Criterio de aceptación
- Flujo completo funciona de extremo a extremo
- No hay errores en consola del navegador
- Responsive en móvil y desktop
- Todos los formularios validan correctamente

---

## Resumen de fases

| Fase | Descripción | Dependencia |
|---|---|---|
| 1 | Configuración + Thymeleaf | Ninguna |
| 2 | Services (lógica de negocio) | Fase 1 |
| 3 | Controllers REST + Thymeleaf | Fase 2 |
| 4 | Spring Security (auth) | Fase 3 |
| 5 | Landing + Auth frontend | Fase 4 |
| 6 | Dashboard Cliente | Fase 5 |
| 7 | Dashboard Veterinario | Fase 5 |
| 8 | Lógica avanzada | Fase 3 |
| 9 | Funcionalidades avanzadas | Fase 6, 7, 8 |
| 10 | Pulido y pruebas | Todas |

---

## Stack tecnológico final

```
Frontend:  Thymeleaf + Bootstrap 5.3.8 + Bootstrap Icons + JavaScript vanilla
Backend:   Spring Boot 4.1.1 + Spring Data JPA + Spring Security + Thymeleaf
Database:  PostgreSQL 16 (Docker Compose)
Build:     Maven + multi-stage Docker
```

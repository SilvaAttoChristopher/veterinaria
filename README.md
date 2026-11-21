# Sistema de Gestión Veterinaria

Aplicativo web para la gestión integral de una veterinaria, desarrollado con Spring Boot y PostgreSQL.

## Tecnologías

- **Backend:** Spring Boot 4.1.1, Java 17, Spring Data JPA, Hibernate, Bean Validation
- **Base de datos:** PostgreSQL 16
- **Frontend:** Thymeleaf + Bootstrap 5 + Bootstrap Icons (JavaScript vanilla)
- **Infraestructura:** Docker Compose (multi-stage build)

## Modelo de datos

Existe una **única tabla `usuario`**. El rol se determina por `usuario.id_rol` y los
campos específicos de cada rol son columnas nullable:

| Rol | Campo propio | Restricción |
|---|---|---|
| `CLIENTE` | `dni` | Obligatorio (validado en Java). Índice único parcial: `ux_usuario_dni` |
| `VETERINARIO` | `especialidad` | Opcional |
| `ADMIN` | — | Sin campos específicos |

> **Nota sobre la unicidad del DNI:** se usa un índice único parcial
> (`WHERE dni IS NOT NULL`) para permitir varios `NULL` (administradores y
> veterinarios) sin perder la garantía de unicidad entre clientes.
> La obligatoriedad del DNI para el rol `CLIENTE` se valida en la capa de servicio,
> no mediante `NOT NULL`, porque la columna es compartida.

Este diseño sustituye a la herencia JPA `JOINED` (`USUARIO` → `CLIENTE`/`VETERINARIO`),
que obligaba a migrar filas entre tablas hijas al cambiar el rol de un usuario.

Las referencias desde el resto del modelo apuntan directamente a `usuario(id_usuario)`:
`mascota.id_cliente`, `boleta.id_cliente`, `cita.id_veterinario`, `horario.id_veterinario`
y `historial.id_veterinario`.

## Inicio rápido

### Con Docker (recomendado)

```bash
docker compose up --build
```

- App: `http://localhost:8080`
- PostgreSQL: `localhost:5432` (usuario: `postgres`, contraseña: `postgres`)

La primera ejecución crea el esquema (`init.sql`) y siembra los datos de prueba.

> Para regenerar la base de datos desde cero:
> `docker compose down -v && docker compose up --build`

### Local

1. Levantar PostgreSQL con Docker:

```bash
docker compose up db -d
```

2. Compilar y ejecutar:

```bash
./mvnw spring-boot:run
```

## Usuarios de prueba

| Rol | Usuario | Contraseña |
|---|---|---|
| Administrador | `admin@vet.com` | `admin123` |
| Cliente | `cliente@test.com` | `cliente123` |
| Veterinario | `vet@test.com` | `vet123` |

## Estructura del proyecto

```
src/main/java/com/example/veterinaria/
├── modelo/         # Entidades JPA
├── repository/     # Repositorios Spring Data
├── service/        # Lógica de negocio
├── controller/     # Controladores REST + PageController (vistas)
├── config/         # DataInitializer (datos de prueba)
└── security/       # WebSecurityConfig, CustomUserDetailsService

src/main/resources/
├── templates/
│   ├── index.html, staff.html
│   ├── fragments/  # layout (público), dashboard-layout (sidebars por rol)
│   └── dashboard/  # páginas de cliente, veterinario y admin
├── static/         # css, js, imagenes
└── db/init.sql     # Esquema inicial
```

## Paneles

| Panel | Ruta | Rol |
|---|---|---|
| Cliente | `/dashboard/perfil` | `CLIENTE` |
| Veterinario | `/dashboard/perfil-medico` | `VETERINARIO` |
| Administración | `/dashboard/admin` | `ADMIN` |

El panel de administración incluye resumen con indicadores, gestión de usuarios
(rol y estado), veterinarios, servicios, roles y permisos, y boletas.

### Reportes

| Reporte | Ruta | API |
|---|---|---|
| Citas (filtro por fechas, estado y veterinario) | `/dashboard/admin/citas` | `GET /api/admin/citas` |
| Historial médico de una mascota | `/dashboard/admin/historial?mascota={id}` | `GET /api/mascotas/{id}` + `GET /api/historial/mascota/{id}` |

Los dos reportes se pueden **exportar a CSV** (separado por `;`, compatible con
Excel en español) o **imprimir / guardar como PDF** desde el navegador.

Las acciones de crear y editar se realizan en **páginas independientes**
(por ejemplo `/dashboard/registrar-mascota`, `/dashboard/admin/editar-usuario/{id}`),
no en modales.

## Control de acceso

`WebSecurityConfig` aplica autorización por rol. Reglas principales:

- **Público:** `/`, `/staff`, recursos estáticos, `POST /api/auth/login`, `POST /api/auth/registro`
- **Sólo `ADMIN`:** `/dashboard/admin/**`, `/api/usuarios/**`, `/api/roles/**`, `/api/permisos/**`,
  `/api/boletas/**`, y cualquier `POST`/`PUT`/`DELETE` sobre `/api/servicios` y `/api/vacunas`
- **Sólo `ADMIN` (borrados):** `DELETE` sobre mascotas, citas, horarios, historiales,
  cartillas, vacunaciones y notificaciones
- **Sólo `VETERINARIO`:** páginas de perfil médico, citas y horarios
- **Sólo `CLIENTE`:** páginas de mascotas, citas y cartilla
- **Resto:** cualquier usuario autenticado

El acceso denegado devuelve `403` con JSON en `/api/**` y redirige al inicio del
propio rol en las páginas.

> **Pendiente de endurecer antes de producción:** CSRF está deshabilitado
> (`csrf.disable()`), las sesiones se guardan en memoria (se pierden al reiniciar
> el contenedor) y `dni` es obligatorio sólo a nivel de aplicación.

## Entidades

| Entidad | Descripción |
|---|---|
| Usuario | Cuenta de acceso (username, password, nombres, apellidos, dni, especialidad, rol) |
| Mascota | Animal registrado por un cliente |
| Cartilla | Cartilla de vacunación (1:1 con mascota) |
| Vacuna | Catálogo de vacunas |
| Vacunación | Aplicación concreta de una vacuna |
| Horario | Horarios disponibles de un veterinario |
| Historial | Atención clínica de una mascota |
| Servicio | Servicio ofrecido por la veterinaria |
| Cita | Reserva de atención |
| Boleta | Comprobante de pago |
| DetalleBoleta | Servicios incluidos en una boleta |
| Rol | Tipo de usuario |
| Permiso | Permisos del sistema |
| Notificación | Notificaciones internas |

## Scripts SQL

- `modelo_er_veterinaria.sql` — Modelo ER de referencia
- `src/main/resources/db/init.sql` — Script de inicialización (Docker)

## Más información

Ver `contexto_aplicativo_veterinaria.md` para el contexto completo del proyecto,
requerimientos funcionales, reglas de negocio y API REST.

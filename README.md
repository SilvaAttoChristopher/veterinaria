# Sistema de Gestión Veterinaria

Aplicativo web para la gestión integral de una veterinaria, desarrollado con Spring Boot y PostgreSQL.

## Tecnologías

- **Backend:** Spring Boot 4.1.1, Java 17, Spring Data JPA, Hibernate, Bean Validation
- **Base de datos:** PostgreSQL 16
- **Pagos:** Stripe Checkout (pasarela de pago alojada, moneda PEN)
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

## Pagos con Stripe

Al agendar una cita el sistema cobra el precio del servicio con **Stripe Checkout**
y la cita sólo queda `PROGRAMADA` cuando el pago es aprobado.

### Flujo

1. `POST /api/pagos/checkout` crea la cita con estado `PENDIENTE_PAGO` (reserva el
   horario) y una sesión de Checkout; el navegador se redirige a Stripe.
2. El cliente paga en la página de Stripe y vuelve a `/pago/exito` (o `/pago/cancelado`).
3. **`POST /api/pagos/webhook`** (firma verificada) confirma el pago → la cita pasa a
   `PROGRAMADA` y el `Pago` a `PAGADO`. Es la vía definitiva: funciona aunque el
   navegador no vuelva.
4. Si el pago se abandona, la sesión expira (`checkout.session.expired`) y la
   reserva se libera. También se libera sola a los 30 minutos y desde el botón
   *Cancelar* de "Mis citas".

La creación directa de citas (`POST /api/citas`) quedó reservada al administrador,
para que nadie pueda esquivar el pago; una cita `PENDIENTE_PAGO` no puede marcarse
como `PROGRAMADA` desde el formulario.

### Configuración

| Variable de entorno | Propiedad | Descripción |
|---|---|---|
| `STRIPE_API_KEY` | `stripe.api-key` | Clave secreta (`sk_test_...` / `sk_live_...`) |
| `STRIPE_WEBHOOK_SECRET` | `stripe.webhook-secret` | Secreto del endpoint (`whsec_...`) |
| `STRIPE_BASE_URL` | `stripe.base-url` | **Opcional.** Fija la URL pública de los retornos |

`STRIPE_BASE_URL` no hace falta configurarlo: la app arma los enlaces de retorno
(`success_url` / `cancel_url`) con la URL de la misma petición que crea la sesión,
respetando los encabezados `X-Forwarded-Proto` y `X-Forwarded-Host` del proxy.
Por eso funciona aunque el dominio del deploy cambie. Definirla sólo tiene sentido
si se quiere forzar un dominio distinto del que llega la petición.

Sin clave, el cobro responde `503` y el resto de la app sigue funcionando.

```bash
cp .env.example .env      # y completa tus claves (el .env no se commitea)
./run.sh                  # arranca cargando las variables del .env
```

También sirve exportarlas a mano:

```bash
export STRIPE_API_KEY=sk_test_...
export STRIPE_WEBHOOK_SECRET=whsec_...
./mvnw spring-boot:run
```

### Probar en local

```bash
# Expone el webhook de Stripe hacia el servidor local
stripe listen --forward-to localhost:8080/api/pagos/webhook
# Copia el whsec_... que imprime y úsalo como STRIPE_WEBHOOK_SECRET
```

> **Atención con el webhook:** a diferencia de los retornos (que se deducen de la
> petición), la URL del endpoint de webhook vive en el dashboard de Stripe y es
> fija. Si cambia el dominio del hay que editar el endpoint en
> *Developers → Webhooks* o volver a correr `stripe listen` en local.

| Endpoint | Descripción |
|---|---|
| `POST /api/pagos/checkout` | Crea la reserva y devuelve la URL de pago |
| `POST /api/pagos/webhook` | Eventos de Stripe (público, firma verificada) |
| `GET /pago/exito` | Retorno exitoso: confirma y muestra el resultado |
| `GET /pago/cancelado` | Retorno cancelado: libera la reserva |

Tarjetas de prueba de Stripe: `4242 4242 4242 4242` (aprobada),
`4000 0000 0000 0002` (rechazada). Fecha futura y CVC cualquiera.

### Migración de la base de datos

`init.sql` ya incluye estos cambios para instalaciones nuevas. En una base ya
existente hay que aplicarlos a mano una sola vez:

```sql
-- 1) El nuevo estado de reserva de pago
ALTER TABLE cita DROP CONSTRAINT chk_cita_estado;
ALTER TABLE cita ADD CONSTRAINT chk_cita_estado
    CHECK (estado IN ('PROGRAMADA', 'ATENDIDA', 'CANCELADA', 'NO_ASISTIO', 'PENDIENTE_PAGO'));

-- 2) El horario único pasa a índice PARCIAL: dos citas activas no pueden compartir
--    horario, pero una CANCELADA lo libera para poder reagendarlo.
ALTER TABLE cita DROP CONSTRAINT uq_cita_veterinario_fecha_hora;
-- (si la restricción la creó Hibernate se llama uk<cadena>: ver pg_constraint)
CREATE UNIQUE INDEX uq_cita_veterinario_fecha_hora
    ON cita (id_veterinario, fecha, hora) WHERE estado <> 'CANCELADA';
```

La tabla `pago` la crea Hibernate al arrancar (`ddl-auto=update`); `init.sql`
también la incluye.

## Control de acceso

`WebSecurityConfig` aplica autorización por rol. Reglas principales:

- **Público:** `/`, `/staff`, recursos estáticos, `POST /api/auth/login`, `POST /api/auth/registro`,
  `POST /api/pagos/webhook` (lo llama Stripe con su firma)
- **Sólo `ADMIN`:** `/dashboard/admin/**`, `/api/usuarios/**`, `/api/roles/**`, `/api/permisos/**`,
  `/api/boletas/**`, y cualquier `POST`/`PUT`/`DELETE` sobre `/api/servicios` y `/api/vacunas`
- **Sólo `ADMIN` (borrados):** `DELETE` sobre mascotas, citas, horarios, historiales,
  cartillas, vacunaciones y notificaciones
- **Sólo `VETERINARIO`:** páginas de perfil médico, citas y horarios
- **Sólo `CLIENTE`:** páginas de mascotas, citas y cartilla
- **Resto:** cualquier usuario autenticado (incluye `/pago/exito` y `/pago/cancelado`,
  y `/api/pagos/**` para `CLIENTE` y `ADMIN`)

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
| Pago | Cobro de una cita contra Stripe (sesión, monto, estado) |
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

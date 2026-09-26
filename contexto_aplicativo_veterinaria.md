# Contexto de desarrollo — Aplicativo web de gestión veterinaria

## 1. Descripción general

El proyecto consiste en desarrollar un **aplicativo web para la gestión de una veterinaria**, orientado a centralizar la información de clientes, mascotas, citas, atención veterinaria, cartillas de vacunación, servicios y boletas.

El sistema debe permitir gestionar de forma organizada los registros definidos por los requerimientos funcionales proporcionados. Los módulos funcionales considerados son: Personas y Acceso, Mascota, Citas, Usuarios, Cartilla y Notificaciones.

El modelo de datos desarrollado para este contexto incorpora además las entidades necesarias para representar veterinarios, horarios, servicios, historial de atención y facturación.

### Herencia de entidades

`USUARIO` es la tabla padre de una herencia JPA `JOINED`. `CLIENTE` y `VETERINARIO` son hijos que comparten los campos base de `USUARIO` (nombres, apellidos, telefono, correo, fecha_registro, estado) y agregan atributos propios.

```text
USUARIO (padre)
├── CLIENTE (hijo)   → agrega: dni
└── VETERINARIO (hijo) → agrega: especialidad
```

El tipo de usuario se determina por el `ROL` asignado: `CLIENTE`, `VETERINARIO` o `ADMIN`.

---

## 2. Tecnologías

### Backend

- **Spring Boot 4.1.1**
- Java 17
- Spring Web MVC
- Spring Data JPA
- Hibernate/JPA
- Bean Validation (Hibernate Validator)

### Base de datos

- **PostgreSQL 16**
- Modelo relacional con herencia JOINED
- Claves primarias y foráneas
- Restricciones de integridad

### Infraestructura

- **Docker Compose** con PostgreSQL + Spring Boot
- Multi-stage build (eclipse-temurin:17)
- Healthcheck para PostgreSQL

### Arquitectura

Se utilizará una arquitectura por capas:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Entity
    ↓
PostgreSQL
```

Responsabilidades:

- **Controller:** recibe solicitudes HTTP y expone la API REST.
- **Service:** contiene la lógica de negocio.
- **Repository:** acceso a datos mediante Spring Data JPA.
- **Entity:** representa las tablas del modelo relacional.
- **DTO:** representa los datos de entrada y salida de la API.

La lógica de negocio no debe colocarse directamente en los controladores.

---

# 3. Objetivo funcional

El aplicativo debe permitir gestionar como mínimo:

1. Usuarios (con roles: Cliente, Veterinario, Admin).
2. Clientes (hereda de Usuario, agrega DNI).
3. Mascotas asociadas a clientes.
4. Cartillas de vacunación.
5. Vacunas y aplicaciones de vacunas.
6. Historial de atención de las mascotas.
7. Veterinarios (hereda de Usuario, agrega especialidad).
8. Horarios de los veterinarios.
9. Citas.
10. Servicios veterinarios.
11. Boletas y detalle de boletas.
12. Roles y permisos.
13. Notificaciones relacionadas con citas y vacunaciones.

---

# 4. Requerimientos funcionales de referencia

## 4.1. Personas y acceso

- Registrar los datos de un cliente.
- Modificar los datos de un cliente registrado.
- Consultar los datos de un cliente.
- Buscar clientes por nombre, DNI o teléfono.

## 4.2. Mascotas

- Registrar una mascota asociándola a un cliente.
- Registrar nombre, especie, raza, sexo y fecha de nacimiento.
- Modificar los datos de una mascota.
- Consultar información e historial de atención de una mascota.
- Generar un carnet a partir de los datos de la mascota.

## 4.3. Citas

- Registrar una cita para una mascota.
- Asignar fecha y hora.
- Verificar disponibilidad del horario antes de registrar una cita.
- Modificar y cancelar una cita.
- Consultar citas programadas.

## 4.4. Usuarios

- Registrar usuarios del sistema.
- Modificar datos de usuarios.
- Asignar roles o permisos.
- Permitir iniciar sesión mediante usuario y contraseña.
- Restringir funcionalidades según los permisos del usuario.

## 4.5. Cartilla

- Registrar vacunas aplicadas a una mascota.
- Registrar fecha de aplicación.
- Registrar próxima dosis.
- Consultar historial de vacunación.
- Identificar vacunas próximas a vencer o pendientes.

## 4.6. Notificaciones

- Generar notificaciones para citas próximas.
- Generar notificaciones para próximas vacunaciones.
- Consultar notificaciones.
- Marcar notificaciones como leídas.
- Visualizar notificaciones pendientes.

---

# 5. Modelo conceptual de entidades

El modelo principal queda organizado de la siguiente manera:

```text
USUARIO (padre JOINED)
├── CLIENTE  1 ──────── N MASCOTA
└── VETERINARIO 1 ──── N HISTORIAL
                  1 ──── N HORARIO
                  1 ──── N CITA

MASCOTA 1 ──────── 1 CARTILLA
CARTILLA 1 ─────── N VACUNACION
VACUNA 1 ───────── N VACUNACION

MASCOTA 1 ──────── N HISTORIAL
MASCOTA 1 ──────── N CITA
SERVICIO 1 ─────── N CITA

CLIENTE 1 ──────── N BOLETA
BOLETA 1 ───────── N DETALLE_BOLETA
SERVICIO 1 ─────── N DETALLE_BOLETA

USUARIO 1 ──────── N NOTIFICACION

ROL N ──────────── N PERMISO (via ROL_PERMISO)
```

## Relación Cita–Servicio

Por decisión de diseño del proyecto, la relación **CITA–SERVICIO se mantiene directamente**:

```text
SERVICIO 1 ─────── N CITA
```

Por tanto, `cita` contiene `id_servicio`. No se debe convertir automáticamente esta relación en una tabla intermedia sin revisar el modelo funcional.

---

# 6. Entidades y atributos

## 6.1. Usuario (tabla padre)

Entidad padre de la herencia `JOINED`. Contiene los campos compartidos por Cliente y Veterinario.

```text
Usuario
-------------------------
id_usuario        PK
username          UNIQUE
password
nombres
apellidos
telefono
correo
fecha_registro
estado            ACTIVO | INACTIVO
id_rol            FK → Rol
```

Convenciones JPA:

```java
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;
    // ...
}
```

## 6.2. Cliente (hijo de Usuario)

Hereda todos los campos de `Usuario`. Agrega únicamente `dni`.

```text
Cliente
-------------------------
id_cliente   PK/FK → usuario
dni          UNIQUE
```

Convención JPA:

```java
@Entity
@Table(name = "cliente")
@PrimaryKeyJoinColumn(name = "id_cliente")
public class Cliente extends Usuario {
    private String dni;
}
```

Relaciones:

```text
Cliente 1 ───── N Mascota
Cliente 1 ───── N Boleta
```

## 6.3. Veterinario (hijo de Usuario)

Hereda todos los campos de `Usuario`. Agrega únicamente `especialidad`.

```text
Veterinario
-------------------------
id_veterinario  PK/FK → usuario
especialidad
```

Convención JPA:

```java
@Entity
@Table(name = "veterinario")
@PrimaryKeyJoinColumn(name = "id_veterinario")
public class Veterinario extends Usuario {
    private String especialidad;
}
```

Relaciones:

```text
Veterinario 1 ───── N Horario
Veterinario 1 ───── N Cita
Veterinario 1 ───── N Historial
```

## 6.4. Mascota

Representa al animal registrado por un cliente.

```text
Mascota
-------------------------
id_mascota          PK
id_cliente          FK → cliente
nombre
especie
raza
sexo
fecha_nacimiento
```

Relaciones:

```text
Mascota N ───── 1 Cliente
Mascota 1 ───── 1 Cartilla
Mascota 1 ───── N Historial
Mascota 1 ───── N Cita
```

## 6.5. Cartilla

Representa la cartilla de vacunación de una mascota.

```text
Cartilla
-------------------------
id_cartilla         PK
id_mascota          FK UNIQUE
fecha_creacion
observaciones
```

La restricción `UNIQUE` sobre `id_mascota` representa una relación 1:1 entre mascota y cartilla.

## 6.6. Vacuna

Catálogo de tipos de vacuna.

```text
Vacuna
-------------------------
id_vacuna           PK
nombre              UNIQUE
descripcion
vigente
```

## 6.7. Vacunación

Representa una aplicación concreta de una vacuna.

```text
Vacunacion
-------------------------
id_vacunacion       PK
id_cartilla         FK
id_vacuna           FK
fecha_aplicacion
proxima_dosis
observaciones
```

Relaciones:

```text
Cartilla 1 ───── N Vacunacion
Vacuna   1 ───── N Vacunacion
```

## 6.8. Horario

Representa los horarios disponibles de un veterinario.

```text
Horario
-------------------------
id_horario          PK
id_veterinario     FK → veterinario
dia_semana
hora_inicio
hora_fin
activo
```

Relación:

```text
Veterinario 1 ───── N Horario
```

## 6.9. Historial

Representa una atención o registro clínico asociado con una mascota.

```text
Historial
-------------------------
id_historial        PK
id_mascota         FK
id_veterinario     FK → veterinario
fecha_atencion
motivo
diagnostico
tratamiento
observaciones
```

Relaciones:

```text
Mascota 1 ───── N Historial
Veterinario 1 ─ N Historial
```

Los atributos del historial son una decisión de diseño para cubrir la necesidad de consultar el historial de atención; los requisitos proporcionados no detallan todos sus campos.

## 6.10. Servicio

Representa un servicio ofrecido por la veterinaria.

```text
Servicio
-------------------------
id_servicio         PK
nombre             UNIQUE
descripcion
precio
activo
```

Relaciones:

```text
Servicio 1 ───── N Cita
Servicio 1 ───── N Detalle_Boleta
```

## 6.11. Cita

Representa una reserva de atención para una mascota.

```text
Cita
-------------------------
id_cita             PK
id_mascota          FK
id_veterinario      FK → veterinario
id_servicio         FK
fecha
hora
estado
motivo
```

Relaciones:

```text
Mascota      1 ───── N Cita
Veterinario  1 ───── N Cita
Servicio     1 ───── N Cita
```

Estados propuestos:

```text
PROGRAMADA
ATENDIDA
CANCELADA
NO_ASISTIO
```

La disponibilidad puede validarse considerando como mínimo veterinario, fecha y hora. El modelo propuesto usa:

```text
UNIQUE(id_veterinario, fecha, hora)
```

## 6.12. Boleta

Representa el comprobante asociado a un cliente.

```text
Boleta
-------------------------
id_boleta           PK
id_cliente          FK → cliente
fecha_emision
subtotal
igv
total
estado
```

## 6.13. Detalle de Boleta

Representa los servicios incluidos en una boleta.

```text
Detalle_Boleta
-------------------------
id_detalle_boleta   PK
id_boleta           FK
id_servicio         FK
cantidad
precio_unitario
subtotal
```

Relaciones:

```text
Boleta   1 ───── N Detalle_Boleta
Servicio 1 ───── N Detalle_Boleta
```

## 6.14. Rol

Define el tipo de usuario en el sistema.

```text
Rol
-------------------------
id_rol       PK
nombre       UNIQUE
```

Roles iniciales:

```text
ADMIN
CLIENTE
VETERINARIO
```

## 6.15. Permiso

Define permisos individuales del sistema.

```text
Permiso
-------------------------
id_permiso    PK
nombre        UNIQUE
descripcion
```

## 6.16. Rol-Permiso (N:M)

Tabla intermedia para la relación N:M entre Rol y Permiso.

```text
Rol_Permiso
-------------------------
id_rol      PK, FK
id_permiso  PK, FK
```

## 6.17. Notificación

Representa notificaciones internas del sistema.

```text
Notificacion
-------------------------
id_notificacion   PK
id_usuario        FK → usuario
tipo
mensaje
fecha
estado_leida
```

Tipos iniciales:

```text
CITA_PROXIMA
VACUNACION_PROXIMA
```

Operaciones:

```text
consultar notificaciones
marcar como leída
consultar pendientes
```

---

# 7. Usuarios y seguridad

Los requisitos contemplan usuarios, roles, permisos, autenticación y control de acceso.

Estructura:

```text
USUARIO N ───── 1 ROL
ROL N ───────── N PERMISO (via ROL_PERMISO)
```

### Herencia de usuarios

`USUARIO` es la entidad padre. `CLIENTE` y `VETERINARIO` son hijos con `@Inheritance(JOINED)`:

- **Cliente:** hereda campos de Usuario + agrega `dni`.
- **Veterinario:** hereda campos de Usuario + agrega `especialidad`.
- **Admin:** solo existe en la tabla `usuario` (sin tabla hija).

El `ROL` determina el tipo de usuario:

| Rol | Tabla adicional |
|---|---|
| CLIENTE | cliente |
| VETERINARIO | veterinario |
| ADMIN | ninguna |

### Convenciones JPA

- `Cliente` usa `@PrimaryKeyJoinColumn(name = "id_cliente")` → FK a `usuario.id_usuario`.
- `Veterinario` usa `@PrimaryKeyJoinColumn(name = "id_veterinario")` → FK a `usuario.id_usuario`.
- En repositorios, las rutas depropiedad usan `idUsuario` (no `idCliente`/`idVeterinario`).

Las contraseñas no deben almacenarse en texto plano.

---

# 8. Docker Compose

El proyecto incluye `docker-compose.yml` con dos servicios:

### PostgreSQL

- Imagen: `postgres:16-alpine`
- Puerto: `5432`
- Base de datos: `veterinaria`
- Usuario: `postgres` / Contraseña: `postgres`
- Init script: `src/main/resources/db/init.sql`
- Healthcheck habilitado

### Spring Boot App

- Build multi-stage: `eclipse-temurin:17-jdk` (build) → `eclipse-temurin:17-jre` (runtime)
- Puerto: `8080`
- Depende de `db` con `condition: service_healthy`
- Variables de entorno para conexión a PostgreSQL

```bash
# Iniciar
docker compose up --build

# Detener
docker compose down
```

---

# 9. Reglas de negocio principales

## Usuarios

- Un usuario tiene un solo rol.
- Un rol puede tener varios permisos.
- El DNI de un cliente debe ser único.
- Un cliente puede registrar varias mascotas.
- Una mascota debe estar asociada a un cliente.

## Mascotas

- Una mascota pertenece a un cliente.
- Una mascota conserva su historial de atención.
- Una mascota puede tener una cartilla.

## Cartilla

- Una mascota tiene una cartilla en el modelo propuesto.
- Una cartilla puede contener varias vacunaciones.
- Cada vacunación corresponde a una vacuna.
- `proxima_dosis`, cuando exista, no debe ser anterior a `fecha_aplicacion`.

## Veterinarios

- Un veterinario puede tener varios horarios.
- Un horario tiene día de semana, hora de inicio y hora de fin.
- `hora_fin` debe ser posterior a `hora_inicio`.

## Citas

- Una cita pertenece a una mascota.
- Una cita pertenece a un veterinario.
- Una cita referencia un servicio.
- Un veterinario no debe tener dos citas para la misma fecha y hora.
- Una cita cancelada no debe considerarse activa al verificar disponibilidad.

## Boletas

- Una boleta pertenece a un cliente.
- Una boleta puede contener varios detalles.
- Cada detalle referencia un servicio.
- El subtotal corresponde a la suma de los detalles.
- El total corresponde al subtotal más el IGV.

---

# 10. Estructura del proyecto Spring Boot

```text
src/main/java/com/example/demo/
│
├── config/
│
├── controller/
│   ├── ClienteController
│   ├── MascotaController
│   ├── CitaController
│   ├── VeterinarioController
│   ├── HorarioController
│   ├── HistorialController
│   ├── CartillaController
│   ├── VacunaController
│   ├── VacunacionController
│   ├── ServicioController
│   ├── BoletaController
│   ├── UsuarioController
│   └── NotificacionController
│
├── dto/
│
├── entity/
│   ├── Usuario          (padre)
│   ├── Cliente          (hijo)
│   ├── Veterinario      (hijo)
│   ├── Rol
│   ├── Permiso
│   ├── RolPermiso
│   ├── RolPermisoId
│   ├── Mascota
│   ├── Cartilla
│   ├── Vacuna
│   ├── Vacunacion
│   ├── Horario
│   ├── Historial
│   ├── Servicio
│   ├── Cita
│   ├── Boleta
│   ├── DetalleBoleta
│   └── Notificacion
│
├── repository/
│   ├── UsuarioRepository
│   ├── ClienteRepository
│   ├── VeterinarioRepository
│   ├── MascotaRepository
│   ├── CartillaRepository
│   ├── VacunaRepository
│   ├── VacunacionRepository
│   ├── HorarioRepository
│   ├── HistorialRepository
│   ├── ServicioRepository
│   ├── CitaRepository
│   ├── BoletaRepository
│   ├── DetalleBoletaRepository
│   ├── RolRepository
│   ├── PermisoRepository
│   ├── RolPermisoRepository
│   └── NotificacionRepository
│
├── service/
├── exception/
└── security/
```

---

# 11. Convenciones JPA

### Herencia (padre)

```java
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;
    // ...
}
```

### Herencia (hijo)

```java
@Entity
@Table(name = "cliente")
@PrimaryKeyJoinColumn(name = "id_cliente")
public class Cliente extends Usuario {
    @NotBlank @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String dni;
}
```

### Repositorios con herencia

Las rutas depropiedad en repositorios deben usar `idUsuario`:

```java
// Correcto
List<Mascota> findByClienteIdUsuario(Long idUsuario);

// Incorrecto (genera error)
List<Mascota> findByClienteIdCliente(Long idCliente);
```

Convenciones generales:

- Usar `Long` para claves primarias.
- Usar `GenerationType.IDENTITY` cuando corresponda al esquema PostgreSQL.
- Definir explícitamente `@Table`.
- Mapear las relaciones con JPA conforme al modelo.
- Evitar relaciones bidireccionales innecesarias.
- Preferir DTOs para las respuestas de la API.
- No exponer campos sensibles de usuario mediante DTOs públicos.

---

# 12. API REST propuesta

## Usuarios

```http
GET    /api/usuarios
GET    /api/usuarios/{id}
POST   /api/usuarios
PUT    /api/usuarios/{id}
DELETE /api/usuarios/{id}
```

## Clientes

```http
GET    /api/clientes
GET    /api/clientes/{id}
POST   /api/clientes
PUT    /api/clientes/{id}
DELETE /api/clientes/{id}
GET    /api/clientes/buscar?criterio=
```

## Mascotas

```http
GET    /api/mascotas
GET    /api/mascotas/{id}
POST   /api/mascotas
PUT    /api/mascotas/{id}
DELETE /api/mascotas/{id}
GET    /api/clientes/{id}/mascotas
```

## Citas

```http
GET    /api/citas
GET    /api/citas/{id}
POST   /api/citas
PUT    /api/citas/{id}
DELETE /api/citas/{id}
GET    /api/citas/disponibilidad
```

## Veterinarios y horarios

```http
GET    /api/veterinarios
POST   /api/veterinarios
PUT    /api/veterinarios/{id}
GET    /api/veterinarios/{id}/horarios
POST   /api/veterinarios/{id}/horarios
PUT    /api/horarios/{id}
DELETE /api/horarios/{id}
```

## Historial

```http
GET  /api/mascotas/{id}/historial
POST /api/historial
```

## Cartilla y vacunas

```http
GET  /api/mascotas/{id}/cartilla
POST /api/cartillas
GET  /api/cartillas/{id}/vacunaciones
POST /api/vacunaciones
GET  /api/vacunas
POST /api/vacunas
```

## Servicios

```http
GET  /api/servicios
POST /api/servicios
PUT  /api/servicios/{id}
```

## Boletas

```http
GET  /api/boletas
GET  /api/boletas/{id}
POST /api/boletas
GET  /api/clientes/{id}/boletas
```

## Notificaciones

```http
GET   /api/notificaciones
PATCH /api/notificaciones/{id}/leida
GET   /api/notificaciones/pendientes
```

---

# 13. Validaciones

Utilizar Bean Validation para validar las entradas antes de persistirlas.

Ejemplos:

```java
@NotBlank
@Size(max = 80)
private String nombres;

@NotBlank
@Size(max = 20)
private String dni;

@Email
private String correo;

@NotNull
private LocalDate fecha;

@DecimalMin(value = "0.0")
private BigDecimal precio;
```

---

# 14. Manejo de excepciones

Respuestas recomendadas:

```text
400 BAD_REQUEST
Datos inválidos.

404 NOT_FOUND
Recurso no encontrado.

409 CONFLICT
El horario ya está ocupado.

500 INTERNAL_SERVER_ERROR
Error interno no controlado.
```

Se recomienda un manejador global:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
}
```

---

# 15. Integridad y base de datos

PostgreSQL debe utilizar restricciones para proteger la consistencia:

- `PRIMARY KEY`
- `FOREIGN KEY`
- `UNIQUE`
- `NOT NULL`
- `CHECK`
- `ON DELETE CASCADE` / `RESTRICT` según relación
- índices en claves foráneas y campos de consulta frecuente.

El script SQL asociado (`modelo_er_veterinaria.sql` y `src/main/resources/db/init.sql`) contiene la estructura correspondiente al modelo desarrollado.

---

# 16. Rendimiento, seguridad y calidad

Los requisitos no funcionales proporcionados establecen que las operaciones de consulta y registro deben responder en un máximo de **3 segundos bajo condiciones normales de uso**.

También se requiere:

- autenticación para funciones restringidas;
- almacenamiento seguro de contraseñas;
- control de acceso por rol;
- protección de información personal;
- integridad de los datos;
- respaldo y recuperación de la información;
- separación clara de responsabilidades;
- código organizado y documentado;
- validación de datos antes de almacenarlos.

---

# 17. Aspectos todavía no definidos

Los requisitos proporcionados no detallan completamente algunos aspectos. No deben asumirse como requisitos cerrados sin una decisión posterior.

### Historial

No se especifica el catálogo definitivo de tipos de atención ni todos sus atributos.

### Facturación

No se especifica el flujo exacto de emisión de boletas, numeración ni integración tributaria.

### Usuarios

Se requiere manejo de usuarios, roles y permisos, pero no se define el catálogo final de roles ni permisos.

### Notificaciones

Se requiere generación y consulta de notificaciones, pero no se especifica un canal de entrega externo. Inicialmente pueden tratarse como notificaciones internas del sistema.

### Carnet

Se exige generar un carnet basado en los datos de la mascota, pero no se define el formato final.

---

# 18. Orden sugerido de desarrollo

```text
1. Configuración Spring Boot + PostgreSQL (Docker Compose)
        ↓
2. Entidades JPA (herencia Usuario → Cliente/Veterinario)
        ↓
3. Repositorios
        ↓
4. Clientes y mascotas
        ↓
5. Veterinarios y horarios
        ↓
6. Servicios y citas
        ↓
7. Historial
        ↓
8. Cartilla, vacunas y vacunaciones
        ↓
9. Boletas y detalle de boletas
        ↓
10. Usuarios, roles, permisos y autenticación
        ↓
11. Notificaciones
        ↓
12. Validaciones, errores y pruebas
```

---

# 19. Regla para futuras modificaciones

Cualquier modificación relevante debe mantener consistencia entre:

```text
Requerimientos
      ↓
Modelo ER
      ↓
PostgreSQL (init.sql)
      ↓
Entidades JPA (herencia)
      ↓
Repositorios (rutas con idUsuario)
      ↓
Servicios Spring Boot
      ↓
API REST
```

Antes de agregar, eliminar o cambiar una relación, se debe revisar su impacto en las demás capas.

No introducir entidades o funcionalidades importantes sin comprobar primero si corresponden a un requerimiento o si fueron identificadas explícitamente como una decisión de diseño.

---

# 20. Fuente funcional

Este contexto se basa en el documento de requisitos proporcionado para el sistema de gestión veterinaria. Los requerimientos incluyen operaciones sobre clientes y mascotas, citas, usuarios, cartilla, vacunación y notificaciones, además de requisitos no funcionales de rendimiento, seguridad, usabilidad, disponibilidad, mantenibilidad y compatibilidad.

Los elementos indicados explícitamente como "propuestos" o "decisiones de diseño" no deben interpretarse como requisitos textuales del documento original.

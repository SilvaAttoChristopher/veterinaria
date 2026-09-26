# Sistema de Gestión Veterinaria

Aplicativo web para la gestión integral de una veterinaria, desarrollado con Spring Boot y PostgreSQL.

## Tecnologías

- **Backend:** Spring Boot 4.1.1, Java 17, Spring Data JPA, Hibernate, Bean Validation
- **Base de datos:** PostgreSQL 16
- **Infraestructura:** Docker Compose (multi-stage build)

## Modelo de Herencia

`USUARIO` es la entidad padre con herencia JPA `JOINED`:

```
USUARIO (padre)
├── CLIENTE      → agrega: dni
└── VETERINARIO  → agrega: especialidad
```

El `ROL` determina el tipo de usuario (ADMIN, CLIENTE, VETERINARIO).

## Inicio rápido

### Con Docker (recomendado)

```bash
docker compose up --build
```

- App: `http://localhost:8080`
- PostgreSQL: `localhost:5432` (usuario: `postgres`, contraseña: `postgres`)

### Local

1. Levantar PostgreSQL con Docker:

```bash
docker compose up db -d
```

2. Compilar y ejecutar:

```bash
./mvnw spring-boot:run
```

## Estructura del proyecto

```
src/main/java/com/example/demo/
├── entity/          # Entidades JPA (Usuario, Cliente, Veterinario, etc.)
├── repository/      # Repositorios Spring Data
├── controller/      # Controladores REST
├── service/         # Lógica de negocio
├── dto/             # DTOs de entrada/salida
├── exception/       # Manejo de excepciones
└── security/        # Seguridad y autenticación
```

## Entidades principales

| Entidad | Descripción |
|---|---|
| Usuario | Tabla padre (username, password, nombres, apellidos, etc.) |
| Cliente | Hijo de Usuario (agrega DNI) |
| Veterinario | Hijo de Usuario (agrega especialidad) |
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

Ver `contexto_aplicativo_veterinaria.md` para el contexto completo del proyecto, requerimientos funcionales, reglas de negocio y API REST propuesta.

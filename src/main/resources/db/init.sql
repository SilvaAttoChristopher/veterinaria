-- ============================================================
-- SCHEMA: veterinaria
-- Inicialización automática via docker-compose
-- ============================================================

-- Rol
CREATE TABLE IF NOT EXISTS rol (
    id_rol       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre       VARCHAR(50) NOT NULL UNIQUE
);

-- Permiso
CREATE TABLE IF NOT EXISTS permiso (
    id_permiso    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL UNIQUE,
    descripcion   VARCHAR(200)
);

-- Rol-Permiso (N:M)
CREATE TABLE IF NOT EXISTS rol_permiso (
    id_rol      BIGINT NOT NULL,
    id_permiso  BIGINT NOT NULL,
    PRIMARY KEY (id_rol, id_permiso),
    CONSTRAINT fk_rp_rol
        FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_rp_permiso
        FOREIGN KEY (id_permiso) REFERENCES permiso(id_permiso)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- Usuario (tabla única)
-- dni aplica a CLIENTE; especialidad aplica a VETERINARIO. Ambos nullable
-- porque ADMIN no tiene ninguno de los dos.
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username      VARCHAR(50) NOT NULL UNIQUE,
    password      VARCHAR(200) NOT NULL,
    nombres       VARCHAR(80) NOT NULL,
    apellidos     VARCHAR(100) NOT NULL,
    telefono      VARCHAR(20),
    correo        VARCHAR(120),
    direccion     VARCHAR(200),
    dni           VARCHAR(20),
    especialidad  VARCHAR(100),
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    estado        VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    foto_perfil   VARCHAR(255),
    id_rol        BIGINT NOT NULL,
    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
        ON UPDATE CASCADE ON DELETE RESTRICT
);
-- Unicidad de dni sin impedir varios NULL (ADMINS y veterinarios)
CREATE UNIQUE INDEX IF NOT EXISTS ux_usuario_dni ON usuario(dni) WHERE dni IS NOT NULL;

-- Mascota
CREATE TABLE IF NOT EXISTS mascota (
    id_mascota          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cliente          BIGINT NOT NULL,
    nombre              VARCHAR(80) NOT NULL,
    especie             VARCHAR(40) NOT NULL,
    raza                VARCHAR(80),
    sexo                CHAR(1) CHECK (sexo IN ('M', 'F')),
    fecha_nacimiento    DATE,
    peso                NUMERIC(5,2) CHECK (peso IS NULL OR peso > 0),
    observaciones       VARCHAR(500),
    CONSTRAINT fk_mascota_usuario
        FOREIGN KEY (id_cliente) REFERENCES usuario(id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_mascota_cliente ON mascota(id_cliente);

-- Cartilla
CREATE TABLE IF NOT EXISTS cartilla (
    id_cartilla       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_mascota        BIGINT NOT NULL UNIQUE,
    fecha_creacion    DATE NOT NULL DEFAULT CURRENT_DATE,
    observaciones     TEXT,
    CONSTRAINT fk_cartilla_mascota
        FOREIGN KEY (id_mascota) REFERENCES mascota(id_mascota)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- Vacuna
CREATE TABLE IF NOT EXISTS vacuna (
    id_vacuna       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL UNIQUE,
    descripcion     TEXT,
    vigente         BOOLEAN NOT NULL DEFAULT TRUE
);

-- Vacunación
CREATE TABLE IF NOT EXISTS vacunacion (
    id_vacunacion    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cartilla      BIGINT NOT NULL,
    id_vacuna        BIGINT NOT NULL,
    fecha_aplicacion DATE NOT NULL,
    proxima_dosis    DATE,
    observaciones    TEXT,
    CONSTRAINT fk_vacunacion_cartilla
        FOREIGN KEY (id_cartilla) REFERENCES cartilla(id_cartilla)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_vacunacion_vacuna
        FOREIGN KEY (id_vacuna) REFERENCES vacuna(id_vacuna)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_vacunacion_fechas
        CHECK (proxima_dosis IS NULL OR proxima_dosis >= fecha_aplicacion)
);
CREATE INDEX IF NOT EXISTS idx_vacunacion_cartilla ON vacunacion(id_cartilla);
CREATE INDEX IF NOT EXISTS idx_vacunacion_vacuna ON vacunacion(id_vacuna);

-- Horario
CREATE TABLE IF NOT EXISTS horario (
    id_horario      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_veterinario  BIGINT NOT NULL,
    dia_semana      SMALLINT NOT NULL,
    hora_inicio     TIME NOT NULL,
    hora_fin        TIME NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_horario_usuario
        FOREIGN KEY (id_veterinario) REFERENCES usuario(id_usuario)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT chk_horario_dia
        CHECK (dia_semana BETWEEN 1 AND 7),
    CONSTRAINT chk_horario_horas
        CHECK (hora_fin > hora_inicio)
);
CREATE INDEX IF NOT EXISTS idx_horario_veterinario ON horario(id_veterinario);

-- Historial
CREATE TABLE IF NOT EXISTS historial (
    id_historial      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_mascota        BIGINT NOT NULL,
    id_veterinario    BIGINT NOT NULL,
    fecha_atencion    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo            VARCHAR(200),
    diagnostico       TEXT,
    tratamiento       TEXT,
    observaciones     TEXT,
    CONSTRAINT fk_historial_mascota
        FOREIGN KEY (id_mascota) REFERENCES mascota(id_mascota)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_historial_usuario
        FOREIGN KEY (id_veterinario) REFERENCES usuario(id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_historial_mascota ON historial(id_mascota);
CREATE INDEX IF NOT EXISTS idx_historial_veterinario ON historial(id_veterinario);
CREATE INDEX IF NOT EXISTS idx_historial_fecha ON historial(fecha_atencion);

-- Servicio
CREATE TABLE IF NOT EXISTS servicio (
    id_servicio     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL UNIQUE,
    descripcion     TEXT,
    precio          NUMERIC(10,2) NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_servicio_precio CHECK (precio >= 0)
);

-- Cita
CREATE TABLE IF NOT EXISTS cita (
    id_cita          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_mascota       BIGINT NOT NULL,
    id_veterinario   BIGINT NOT NULL,
    id_servicio      BIGINT NOT NULL,
    fecha            DATE NOT NULL,
    hora             TIME NOT NULL,
    estado           VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA',
    motivo           VARCHAR(200),
    CONSTRAINT fk_cita_mascota
        FOREIGN KEY (id_mascota) REFERENCES mascota(id_mascota)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_cita_usuario
        FOREIGN KEY (id_veterinario) REFERENCES usuario(id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_cita_servicio
        FOREIGN KEY (id_servicio) REFERENCES servicio(id_servicio)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_cita_estado
        CHECK (estado IN ('PROGRAMADA', 'ATENDIDA', 'CANCELADA', 'NO_ASISTIO', 'PENDIENTE_PAGO'))
);
CREATE INDEX IF NOT EXISTS idx_cita_mascota ON cita(id_mascota);
CREATE INDEX IF NOT EXISTS idx_cita_veterinario ON cita(id_veterinario);
CREATE INDEX IF NOT EXISTS idx_cita_servicio ON cita(id_servicio);
CREATE INDEX IF NOT EXISTS idx_cita_fecha ON cita(fecha);
-- Índice único PARCIAL: un horario no puede tener dos citas activas, pero una cita
-- CANCELADA libera el horario para poder reagendarlo.
CREATE UNIQUE INDEX IF NOT EXISTS uq_cita_veterinario_fecha_hora
    ON cita(id_veterinario, fecha, hora) WHERE estado <> 'CANCELADA';

-- Pago (Stripe Checkout)
CREATE TABLE IF NOT EXISTS pago (
    id_pago                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cita                  BIGINT NOT NULL,
    monto                    NUMERIC(10,2) NOT NULL,
    moneda                   VARCHAR(10) NOT NULL,
    estado                   VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_pago               TIMESTAMP,
    stripe_session_id        VARCHAR(255) UNIQUE,
    stripe_payment_intent_id VARCHAR(255),
    CONSTRAINT fk_pago_cita
        FOREIGN KEY (id_cita) REFERENCES cita(id_cita)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_pago_estado
        CHECK (estado IN ('PENDIENTE', 'PAGADO', 'CANCELADO', 'FALLIDO')),
    CONSTRAINT chk_pago_monto CHECK (monto > 0)
);
CREATE INDEX IF NOT EXISTS idx_pago_cita ON pago(id_cita);

-- Boleta
CREATE TABLE IF NOT EXISTS boleta (
    id_boleta        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cliente       BIGINT NOT NULL,
    fecha_emision    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal         NUMERIC(10,2) NOT NULL DEFAULT 0,
    igv              NUMERIC(10,2) NOT NULL DEFAULT 0,
    total            NUMERIC(10,2) NOT NULL DEFAULT 0,
    estado           VARCHAR(20) NOT NULL DEFAULT 'EMITIDA',
    CONSTRAINT fk_boleta_usuario
        FOREIGN KEY (id_cliente) REFERENCES usuario(id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_boleta_montos
        CHECK (subtotal >= 0 AND igv >= 0 AND total >= 0),
    CONSTRAINT chk_boleta_estado
        CHECK (estado IN ('EMITIDA', 'ANULADA', 'PAGADA', 'PENDIENTE'))
);
CREATE INDEX IF NOT EXISTS idx_boleta_cliente ON boleta(id_cliente);
CREATE INDEX IF NOT EXISTS idx_boleta_fecha ON boleta(fecha_emision);

-- Detalle Boleta
CREATE TABLE IF NOT EXISTS detalle_boleta (
    id_detalle_boleta BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_boleta         BIGINT NOT NULL,
    id_servicio       BIGINT NOT NULL,
    cantidad          INTEGER NOT NULL DEFAULT 1,
    precio_unitario   NUMERIC(10,2) NOT NULL,
    subtotal          NUMERIC(10,2) NOT NULL,
    CONSTRAINT fk_detalle_boleta_boleta
        FOREIGN KEY (id_boleta) REFERENCES boleta(id_boleta)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_detalle_boleta_servicio
        FOREIGN KEY (id_servicio) REFERENCES servicio(id_servicio)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio CHECK (precio_unitario >= 0),
    CONSTRAINT chk_detalle_subtotal CHECK (subtotal >= 0)
);
CREATE INDEX IF NOT EXISTS idx_detalle_boleta_boleta ON detalle_boleta(id_boleta);
CREATE INDEX IF NOT EXISTS idx_detalle_boleta_servicio ON detalle_boleta(id_servicio);

-- Notificación
CREATE TABLE IF NOT EXISTS notificacion (
    id_notificacion   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario        BIGINT NOT NULL,
    tipo              VARCHAR(30) NOT NULL,
    mensaje           TEXT NOT NULL,
    fecha             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_leida      BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_notificacion_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON UPDATE CASCADE ON DELETE CASCADE
);

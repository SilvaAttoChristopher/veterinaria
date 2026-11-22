-- ============================================================
-- MODELO ER DE REFERENCIA — VETERINARIA
-- ============================================================
-- Este archivo documenta el esquema. El esquema que se crea realmente
-- en Docker es src/main/resources/db/init.sql (fuente de verdad).
--
-- MODELO DE USUARIOS: tabla única
-- ---------------------------
-- No hay herencia JPA ni tablas hijas. `usuario` guarda todos los
-- tipos de cuenta y el rol se determina por `usuario.id_rol`:
--
--     rol CLIENTE      -> exige dni           (columna nullable)
--     rol VETERINARIO  -> especialidad        (columna nullable)
--     rol ADMIN        -> sin campos propios
--
-- La unicidad del dni se garantiza con un índice único parcial
-- (ux_usuario_dni) para permitir varios NULL entre no clientes.
-- La obligatoriedad del dni para CLIENTE se valida en Java.
--
-- REFERENCIAS A USUARIOS
-- ---------------------
--   mascota.id_cliente       -> usuario(id_usuario)
--   boleta.id_cliente        -> usuario(id_usuario)
--   cita.id_veterinario      -> usuario(id_usuario)
--   horario.id_veterinario   -> usuario(id_usuario)
--   historial.id_veterinario -> usuario(id_usuario)
--   notificacion.id_usuario  -> usuario(id_usuario)
--
-- Este diseño sustituye a la herencia JOINED (USUARIO -> CLIENTE /
-- VETERINARIO), que obligaba a migrar filas entre tablas hijas al
-- cambiar el rol de un usuario y provocaba WrongClassException en
-- Hibernate al actualizar desde JSON.
--
-- Tablas: rol, permiso, rol_permiso, usuario, mascota, cartilla,
--         vacuna, vacunacion, horario, historial, servicio, cita,
--         boleta, detalle_boleta, notificacion
--
-- Regenerar tras un cambio de esquema:
--   docker compose exec -T db pg_dump -U postgres -d veterinaria \
--     --schema-only --no-owner --no-privileges
-- ============================================================

--
-- PostgreSQL database dump
--


-- Dumped from database version 16.15
-- Dumped by pg_dump version 16.15

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: boleta; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.boleta (
    id_boleta bigint NOT NULL,
    id_cliente bigint NOT NULL,
    fecha_emision timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    subtotal numeric(10,2) DEFAULT 0 NOT NULL,
    igv numeric(10,2) DEFAULT 0 NOT NULL,
    total numeric(10,2) DEFAULT 0 NOT NULL,
    estado character varying(20) DEFAULT 'EMITIDA'::character varying NOT NULL,
    CONSTRAINT chk_boleta_estado CHECK (((estado)::text = ANY ((ARRAY['EMITIDA'::character varying, 'ANULADA'::character varying, 'PAGADA'::character varying, 'PENDIENTE'::character varying])::text[]))),
    CONSTRAINT chk_boleta_montos CHECK (((subtotal >= (0)::numeric) AND (igv >= (0)::numeric) AND (total >= (0)::numeric)))
);


--
-- Name: boleta_id_boleta_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.boleta ALTER COLUMN id_boleta ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.boleta_id_boleta_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: cartilla; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cartilla (
    id_cartilla bigint NOT NULL,
    id_mascota bigint NOT NULL,
    fecha_creacion date DEFAULT CURRENT_DATE NOT NULL,
    observaciones text
);


--
-- Name: cartilla_id_cartilla_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.cartilla ALTER COLUMN id_cartilla ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.cartilla_id_cartilla_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: cita; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cita (
    id_cita bigint NOT NULL,
    id_mascota bigint NOT NULL,
    id_veterinario bigint NOT NULL,
    id_servicio bigint NOT NULL,
    fecha date NOT NULL,
    hora time without time zone NOT NULL,
    estado character varying(20) DEFAULT 'PROGRAMADA'::character varying NOT NULL,
    motivo character varying(200),
    CONSTRAINT chk_cita_estado CHECK (((estado)::text = ANY ((ARRAY['PROGRAMADA'::character varying, 'ATENDIDA'::character varying, 'CANCELADA'::character varying, 'NO_ASISTIO'::character varying, 'PENDIENTE_PAGO'::character varying])::text[])))
);


--
-- Name: cita_id_cita_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.cita ALTER COLUMN id_cita ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.cita_id_cita_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: detalle_boleta; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.detalle_boleta (
    id_detalle_boleta bigint NOT NULL,
    id_boleta bigint NOT NULL,
    id_servicio bigint NOT NULL,
    cantidad integer DEFAULT 1 NOT NULL,
    precio_unitario numeric(10,2) NOT NULL,
    subtotal numeric(10,2) NOT NULL,
    CONSTRAINT chk_detalle_cantidad CHECK ((cantidad > 0)),
    CONSTRAINT chk_detalle_precio CHECK ((precio_unitario >= (0)::numeric)),
    CONSTRAINT chk_detalle_subtotal CHECK ((subtotal >= (0)::numeric))
);


--
-- Name: detalle_boleta_id_detalle_boleta_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.detalle_boleta ALTER COLUMN id_detalle_boleta ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.detalle_boleta_id_detalle_boleta_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: historial; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.historial (
    id_historial bigint NOT NULL,
    id_mascota bigint NOT NULL,
    id_veterinario bigint NOT NULL,
    fecha_atencion timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    motivo character varying(200),
    diagnostico text,
    tratamiento text,
    observaciones text
);


--
-- Name: historial_id_historial_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.historial ALTER COLUMN id_historial ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.historial_id_historial_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: horario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.horario (
    id_horario bigint NOT NULL,
    id_veterinario bigint NOT NULL,
    dia_semana smallint NOT NULL,
    hora_inicio time without time zone NOT NULL,
    hora_fin time without time zone NOT NULL,
    activo boolean DEFAULT true NOT NULL,
    CONSTRAINT chk_horario_dia CHECK (((dia_semana >= 1) AND (dia_semana <= 7))),
    CONSTRAINT chk_horario_horas CHECK ((hora_fin > hora_inicio))
);


--
-- Name: horario_id_horario_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.horario ALTER COLUMN id_horario ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.horario_id_horario_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: mascota; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mascota (
    id_mascota bigint NOT NULL,
    id_cliente bigint NOT NULL,
    nombre character varying(80) NOT NULL,
    especie character varying(40) NOT NULL,
    raza character varying(80),
    sexo character(1),
    fecha_nacimiento date,
    peso numeric(5,2),
    observaciones character varying(500),
    CONSTRAINT mascota_peso_check CHECK (((peso IS NULL) OR (peso > (0)::numeric))),
    CONSTRAINT mascota_sexo_check CHECK ((sexo = ANY (ARRAY['M'::bpchar, 'F'::bpchar])))
);


--
-- Name: mascota_id_mascota_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.mascota ALTER COLUMN id_mascota ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.mascota_id_mascota_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: notificacion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notificacion (
    id_notificacion bigint NOT NULL,
    id_usuario bigint NOT NULL,
    tipo character varying(30) NOT NULL,
    mensaje text NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    estado_leida boolean DEFAULT false NOT NULL
);


--
-- Name: notificacion_id_notificacion_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.notificacion ALTER COLUMN id_notificacion ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.notificacion_id_notificacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: permiso; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.permiso (
    id_permiso bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion character varying(200)
);


--
-- Name: permiso_id_permiso_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.permiso ALTER COLUMN id_permiso ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.permiso_id_permiso_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: rol; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rol (
    id_rol bigint NOT NULL,
    nombre character varying(50) NOT NULL
);


--
-- Name: rol_id_rol_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.rol ALTER COLUMN id_rol ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.rol_id_rol_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: rol_permiso; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rol_permiso (
    id_rol bigint NOT NULL,
    id_permiso bigint NOT NULL
);


--
-- Name: servicio; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.servicio (
    id_servicio bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion text,
    precio numeric(10,2) NOT NULL,
    activo boolean DEFAULT true NOT NULL,
    CONSTRAINT chk_servicio_precio CHECK ((precio >= (0)::numeric))
);


--
-- Name: servicio_id_servicio_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.servicio ALTER COLUMN id_servicio ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.servicio_id_servicio_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: usuario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuario (
    id_usuario bigint NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(200) NOT NULL,
    nombres character varying(80) NOT NULL,
    apellidos character varying(100) NOT NULL,
    telefono character varying(20),
    correo character varying(120),
    direccion character varying(200),
    dni character varying(20),
    especialidad character varying(100),
    fecha_registro timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    estado character varying(20) DEFAULT 'ACTIVO'::character varying NOT NULL,
    id_rol bigint NOT NULL
);


--
-- Name: usuario_id_usuario_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.usuario ALTER COLUMN id_usuario ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.usuario_id_usuario_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: vacuna; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.vacuna (
    id_vacuna bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion text,
    vigente boolean DEFAULT true NOT NULL
);


--
-- Name: vacuna_id_vacuna_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.vacuna ALTER COLUMN id_vacuna ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.vacuna_id_vacuna_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: vacunacion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.vacunacion (
    id_vacunacion bigint NOT NULL,
    id_cartilla bigint NOT NULL,
    id_vacuna bigint NOT NULL,
    fecha_aplicacion date NOT NULL,
    proxima_dosis date,
    observaciones text,
    CONSTRAINT chk_vacunacion_fechas CHECK (((proxima_dosis IS NULL) OR (proxima_dosis >= fecha_aplicacion)))
);


--
-- Name: vacunacion_id_vacunacion_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.vacunacion ALTER COLUMN id_vacunacion ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.vacunacion_id_vacunacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: boleta boleta_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.boleta
    ADD CONSTRAINT boleta_pkey PRIMARY KEY (id_boleta);


--
-- Name: cartilla cartilla_id_mascota_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cartilla
    ADD CONSTRAINT cartilla_id_mascota_key UNIQUE (id_mascota);


--
-- Name: cartilla cartilla_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cartilla
    ADD CONSTRAINT cartilla_pkey PRIMARY KEY (id_cartilla);


--
-- Name: cita cita_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cita
    ADD CONSTRAINT cita_pkey PRIMARY KEY (id_cita);


--
-- Name: detalle_boleta detalle_boleta_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.detalle_boleta
    ADD CONSTRAINT detalle_boleta_pkey PRIMARY KEY (id_detalle_boleta);


--
-- Name: historial historial_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historial
    ADD CONSTRAINT historial_pkey PRIMARY KEY (id_historial);


--
-- Name: horario horario_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.horario
    ADD CONSTRAINT horario_pkey PRIMARY KEY (id_horario);


--
-- Name: mascota mascota_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mascota
    ADD CONSTRAINT mascota_pkey PRIMARY KEY (id_mascota);


--
-- Name: notificacion notificacion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notificacion
    ADD CONSTRAINT notificacion_pkey PRIMARY KEY (id_notificacion);


--
-- Name: permiso permiso_nombre_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permiso
    ADD CONSTRAINT permiso_nombre_key UNIQUE (nombre);


--
-- Name: permiso permiso_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permiso
    ADD CONSTRAINT permiso_pkey PRIMARY KEY (id_permiso);


--
-- Name: rol rol_nombre_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT rol_nombre_key UNIQUE (nombre);


--
-- Name: rol_permiso rol_permiso_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rol_permiso
    ADD CONSTRAINT rol_permiso_pkey PRIMARY KEY (id_rol, id_permiso);


--
-- Name: rol rol_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT rol_pkey PRIMARY KEY (id_rol);


--
-- Name: servicio servicio_nombre_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.servicio
    ADD CONSTRAINT servicio_nombre_key UNIQUE (nombre);


--
-- Name: servicio servicio_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.servicio
    ADD CONSTRAINT servicio_pkey PRIMARY KEY (id_servicio);


--
-- Name: cita uq_cita_veterinario_fecha_hora; Type: CONSTRAINT; Schema: public; Owner: -
--
-- Índice único PARCIAL: un horario no puede tener dos citas activas, pero una cita
-- CANCELADA libera el horario para poder reagendarlo.
--

CREATE UNIQUE INDEX uq_cita_veterinario_fecha_hora ON public.cita USING btree (id_veterinario, fecha, hora) WHERE ((estado)::text <> 'CANCELADA'::text);


--
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id_usuario);


--
-- Name: usuario usuario_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_username_key UNIQUE (username);


--
-- Name: vacuna vacuna_nombre_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vacuna
    ADD CONSTRAINT vacuna_nombre_key UNIQUE (nombre);


--
-- Name: vacuna vacuna_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vacuna
    ADD CONSTRAINT vacuna_pkey PRIMARY KEY (id_vacuna);


--
-- Name: vacunacion vacunacion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vacunacion
    ADD CONSTRAINT vacunacion_pkey PRIMARY KEY (id_vacunacion);


--
-- Name: idx_boleta_cliente; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_boleta_cliente ON public.boleta USING btree (id_cliente);


--
-- Name: idx_boleta_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_boleta_fecha ON public.boleta USING btree (fecha_emision);


--
-- Name: idx_cita_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cita_fecha ON public.cita USING btree (fecha);


--
-- Name: idx_cita_mascota; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cita_mascota ON public.cita USING btree (id_mascota);


--
-- Name: idx_cita_servicio; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cita_servicio ON public.cita USING btree (id_servicio);


--
-- Name: idx_cita_veterinario; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cita_veterinario ON public.cita USING btree (id_veterinario);


--
-- Name: idx_detalle_boleta_boleta; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_detalle_boleta_boleta ON public.detalle_boleta USING btree (id_boleta);


--
-- Name: idx_detalle_boleta_servicio; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_detalle_boleta_servicio ON public.detalle_boleta USING btree (id_servicio);


--
-- Name: idx_historial_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_historial_fecha ON public.historial USING btree (fecha_atencion);


--
-- Name: idx_historial_mascota; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_historial_mascota ON public.historial USING btree (id_mascota);


--
-- Name: idx_historial_veterinario; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_historial_veterinario ON public.historial USING btree (id_veterinario);


--
-- Name: idx_horario_veterinario; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_horario_veterinario ON public.horario USING btree (id_veterinario);


--
-- Name: idx_mascota_cliente; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_mascota_cliente ON public.mascota USING btree (id_cliente);


--
-- Name: idx_vacunacion_cartilla; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_vacunacion_cartilla ON public.vacunacion USING btree (id_cartilla);


--
-- Name: idx_vacunacion_vacuna; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_vacunacion_vacuna ON public.vacunacion USING btree (id_vacuna);


--
-- Name: ux_usuario_dni; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ux_usuario_dni ON public.usuario USING btree (dni) WHERE (dni IS NOT NULL);


--
-- Name: boleta fk_boleta_usuario; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.boleta
    ADD CONSTRAINT fk_boleta_usuario FOREIGN KEY (id_cliente) REFERENCES public.usuario(id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: cartilla fk_cartilla_mascota; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cartilla
    ADD CONSTRAINT fk_cartilla_mascota FOREIGN KEY (id_mascota) REFERENCES public.mascota(id_mascota) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: cita fk_cita_mascota; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cita
    ADD CONSTRAINT fk_cita_mascota FOREIGN KEY (id_mascota) REFERENCES public.mascota(id_mascota) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: cita fk_cita_servicio; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cita
    ADD CONSTRAINT fk_cita_servicio FOREIGN KEY (id_servicio) REFERENCES public.servicio(id_servicio) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: cita fk_cita_usuario; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cita
    ADD CONSTRAINT fk_cita_usuario FOREIGN KEY (id_veterinario) REFERENCES public.usuario(id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: detalle_boleta fk_detalle_boleta_boleta; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.detalle_boleta
    ADD CONSTRAINT fk_detalle_boleta_boleta FOREIGN KEY (id_boleta) REFERENCES public.boleta(id_boleta) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: detalle_boleta fk_detalle_boleta_servicio; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.detalle_boleta
    ADD CONSTRAINT fk_detalle_boleta_servicio FOREIGN KEY (id_servicio) REFERENCES public.servicio(id_servicio) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: historial fk_historial_mascota; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historial
    ADD CONSTRAINT fk_historial_mascota FOREIGN KEY (id_mascota) REFERENCES public.mascota(id_mascota) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: historial fk_historial_usuario; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historial
    ADD CONSTRAINT fk_historial_usuario FOREIGN KEY (id_veterinario) REFERENCES public.usuario(id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: horario fk_horario_usuario; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.horario
    ADD CONSTRAINT fk_horario_usuario FOREIGN KEY (id_veterinario) REFERENCES public.usuario(id_usuario) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mascota fk_mascota_usuario; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mascota
    ADD CONSTRAINT fk_mascota_usuario FOREIGN KEY (id_cliente) REFERENCES public.usuario(id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: notificacion fk_notificacion_usuario; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notificacion
    ADD CONSTRAINT fk_notificacion_usuario FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: rol_permiso fk_rp_permiso; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rol_permiso
    ADD CONSTRAINT fk_rp_permiso FOREIGN KEY (id_permiso) REFERENCES public.permiso(id_permiso) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: rol_permiso fk_rp_rol; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rol_permiso
    ADD CONSTRAINT fk_rp_rol FOREIGN KEY (id_rol) REFERENCES public.rol(id_rol) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: usuario fk_usuario_rol; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES public.rol(id_rol) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: vacunacion fk_vacunacion_cartilla; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vacunacion
    ADD CONSTRAINT fk_vacunacion_cartilla FOREIGN KEY (id_cartilla) REFERENCES public.cartilla(id_cartilla) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: vacunacion fk_vacunacion_vacuna; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vacunacion
    ADD CONSTRAINT fk_vacunacion_vacuna FOREIGN KEY (id_vacuna) REFERENCES public.vacuna(id_vacuna) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- PostgreSQL database dump complete
--



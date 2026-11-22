package com.example.veterinaria.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.veterinaria.modelo.Cita;
import com.example.veterinaria.security.Autorizacion;
import com.example.veterinaria.service.CitaService;
import com.example.veterinaria.service.PagoService;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @Autowired
    private PagoService pagoService;

    @GetMapping
    public ResponseEntity<List<Cita>> findAll() {
        /* El listado global queda para el admin: un cliente o un veterinario
           deben usar los endpoints acotados por propietario. */
        if (!Autorizacion.esAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(citaService.findAll());
    }

    /** Citas de un cliente, deduciendo las mascotas que son suyas. */
    @GetMapping("/cliente/{idUsuario}")
    public ResponseEntity<List<Cita>> findByCliente(@PathVariable Long idUsuario) {
        if (!Autorizacion.esPropioOAdmin(idUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(citaService.findByUsuario(idUsuario));
    }

    /** Citas asignadas a un veterinario, de cualquier fecha. */
    @GetMapping("/veterinario/{idUsuario}")
    public ResponseEntity<List<Cita>> findByVeterinario(@PathVariable Long idUsuario) {
        if (!Autorizacion.esPropioOAdmin(idUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(citaService.findByVeterinario(idUsuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> findById(@PathVariable Long id) {
        return citaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/mascota/{idMascota}")
    public List<Cita> findByMascotaIdMascota(@PathVariable Long idMascota) {
        return citaService.findByMascotaIdMascota(idMascota);
    }

    @GetMapping("/veterinario/{idUsuario}/fecha")
    public List<Cita> findByUsuarioIdUsuarioAndFecha(
            @PathVariable Long idUsuario,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return citaService.findByUsuarioIdUsuarioAndFecha(idUsuario, fecha);
    }

    @GetMapping("/estado/{estado}")
    public List<Cita> findByEstado(@PathVariable String estado) {
        return citaService.findByEstado(estado);
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody Cita cita) {
        /* Las citas de los clientes se crean junto con el pago (Stripe Checkout);
           el alta directa queda reservada al administrador. */
        if (!Autorizacion.esAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", "Las citas se crean mediante el pago en linea"));
        }
        try {
            Cita saved = citaService.save(cita);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "Ese horario ya está reservado"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Cita cita) {
        Cita existente = citaService.findById(id).orElse(null);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        /* Se lee el estado antes de guardar: al hacer merge Hibernate actualiza la
           misma instancia managed y el valor viejo quedaría perdido. */
        String estadoAnterior = existente.getEstado();

        /* Una cita pendiente de pago no puede "volverse" programada desde el
           formulario: sólo se confirma con el pago en Stripe, o se cancela. */
        if (PagoService.CITA_PENDIENTE_PAGO.equals(estadoAnterior)) {
            boolean laCancelan = PagoService.CITA_CANCELADA.equals(cita.getEstado());
            if (!laCancelan) {
                cita.setEstado(PagoService.CITA_PENDIENTE_PAGO);
            } else if (!Autorizacion.esPropioOAdmin(existente.getMascota()
                    .getCliente().getIdUsuario())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("mensaje", "No tiene permisos para cancelar esta cita"));
            }
        }

        cita.setIdCita(id);
        try {
            Cita guardada = citaService.save(cita);
            if (PagoService.CITA_CANCELADA.equals(cita.getEstado())
                    && PagoService.CITA_PENDIENTE_PAGO.equals(estadoAnterior)) {
                /* Se canceló una reserva sin pagar: se cierra también la sesión de Stripe. */
                pagoService.cancelarPorCita(id);
            }
            return ResponseEntity.ok(guardada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!citaService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        citaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

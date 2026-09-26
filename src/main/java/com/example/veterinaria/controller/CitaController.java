package com.example.veterinaria.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.example.veterinaria.service.CitaService;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @GetMapping
    public List<Cita> findAll() {
        return citaService.findAll();
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
    public List<Cita> findByVeterinarioIdUsuarioAndFecha(
            @PathVariable Long idUsuario,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return citaService.findByVeterinarioIdUsuarioAndFecha(idUsuario, fecha);
    }

    @GetMapping("/estado/{estado}")
    public List<Cita> findByEstado(@PathVariable String estado) {
        return citaService.findByEstado(estado);
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody Cita cita) {
        try {
            Cita saved = citaService.save(cita);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> update(@PathVariable Long id, @RequestBody Cita cita) {
        if (!citaService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        cita.setIdCita(id);
        Cita saved = citaService.save(cita);
        return ResponseEntity.ok(saved);
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

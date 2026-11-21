package com.example.veterinaria.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.veterinaria.modelo.Historial;
import com.example.veterinaria.security.Autorizacion;
import com.example.veterinaria.service.HistorialService;
import com.example.veterinaria.service.TitularidadService;

@RestController
@RequestMapping("/api/historial")
public class HistorialController {

    @Autowired
    private HistorialService historialService;

    @Autowired
    private TitularidadService titularidadService;

    @GetMapping
    public List<Historial> findAll() {
        return historialService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Historial> findById(@PathVariable Long id) {
        return historialService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/mascota/{idMascota}")
    public ResponseEntity<List<Historial>> findByMascota(@PathVariable Long idMascota) {
        /* El historial clínico sólo lo ven el dueño de la mascota, un veterinario
           o el admin: es información médica de un paciente. */
        var actual = Autorizacion.actual();
        boolean permitido = Autorizacion.esAdmin() || Autorizacion.esVeterinario()
                || (actual != null && titularidadService.esDueño(idMascota, actual.idUsuario()));

        if (!permitido) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(historialService.findByMascotaIdMascotaOrderByFechaAtencionDesc(idMascota));
    }

    @GetMapping("/veterinario/{idUsuario}")
    public ResponseEntity<List<Historial>> findByVeterinario(@PathVariable Long idUsuario) {
        if (!Autorizacion.esPropioOAdmin(idUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(historialService.findByUsuarioIdUsuario(idUsuario));
    }

    @PostMapping
    public ResponseEntity<Historial> save(@RequestBody Historial historial) {
        Historial saved = historialService.save(historial);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Historial> update(@PathVariable Long id, @RequestBody Historial historial) {
        if (!historialService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        historial.setIdHistorial(id);
        Historial saved = historialService.save(historial);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!historialService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        historialService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

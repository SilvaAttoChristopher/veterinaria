package com.example.veterinaria.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.veterinaria.modelo.Notificacion;
import com.example.veterinaria.service.NotificacionService;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping
    public List<Notificacion> findAll() {
        return notificacionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notificacion> findById(@PathVariable Long id) {
        return notificacionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<Notificacion> findByUsuarioIdUsuario(@PathVariable Long idUsuario) {
        return notificacionService.findByUsuarioIdUsuario(idUsuario);
    }

    @GetMapping("/usuario/{idUsuario}/pendientes")
    public List<Notificacion> findByUsuarioIdUsuarioAndEstadoLeidaFalse(@PathVariable Long idUsuario) {
        return notificacionService.findByUsuarioIdUsuarioAndEstadoLeidaFalse(idUsuario);
    }

    @PatchMapping("/{id}/leida")
    public ResponseEntity<Notificacion> marcarComoLeida(@PathVariable Long id) {
        return notificacionService.findById(id)
                .map(notificacion -> {
                    notificacion.setEstadoLeida(true);
                    Notificacion saved = notificacionService.save(notificacion);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Notificacion> save(@RequestBody Notificacion notificacion) {
        Notificacion saved = notificacionService.save(notificacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notificacion> update(@PathVariable Long id, @RequestBody Notificacion notificacion) {
        if (!notificacionService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        notificacion.setIdNotificacion(id);
        Notificacion saved = notificacionService.save(notificacion);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!notificacionService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        notificacionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

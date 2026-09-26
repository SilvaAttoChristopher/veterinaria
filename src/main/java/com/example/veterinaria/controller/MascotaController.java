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

import com.example.veterinaria.modelo.Mascota;
import com.example.veterinaria.service.MascotaService;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    @Autowired
    private MascotaService mascotaService;

    @GetMapping
    public List<Mascota> findAll() {
        return mascotaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mascota> findById(@PathVariable Long id) {
        return mascotaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{idUsuario}")
    public List<Mascota> findByClienteIdUsuario(@PathVariable Long idUsuario) {
        return mascotaService.findByClienteIdUsuario(idUsuario);
    }

    @PostMapping
    public ResponseEntity<Mascota> save(@RequestBody Mascota mascota) {
        Mascota saved = mascotaService.save(mascota);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> update(@PathVariable Long id, @RequestBody Mascota mascota) {
        if (!mascotaService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        mascota.setIdMascota(id);
        Mascota saved = mascotaService.save(mascota);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!mascotaService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        mascotaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

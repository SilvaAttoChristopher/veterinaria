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

import com.example.veterinaria.modelo.Servicio;
import com.example.veterinaria.service.ServicioService;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    @GetMapping
    public List<Servicio> findAll() {
        return servicioService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Servicio> findById(@PathVariable Long id) {
        return servicioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Servicio> save(@RequestBody Servicio servicio) {
        Servicio saved = servicioService.save(servicio);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Servicio> update(@PathVariable Long id, @RequestBody Servicio servicio) {
        if (!servicioService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        servicio.setIdServicio(id);
        Servicio saved = servicioService.save(servicio);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!servicioService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        servicioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

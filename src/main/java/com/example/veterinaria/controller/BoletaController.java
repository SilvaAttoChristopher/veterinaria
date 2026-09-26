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

import com.example.veterinaria.modelo.Boleta;
import com.example.veterinaria.service.BoletaService;

@RestController
@RequestMapping("/api/boletas")
public class BoletaController {

    @Autowired
    private BoletaService boletaService;

    @GetMapping
    public List<Boleta> findAll() {
        return boletaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Boleta> findById(@PathVariable Long id) {
        return boletaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{idUsuario}")
    public List<Boleta> findByClienteIdUsuario(@PathVariable Long idUsuario) {
        return boletaService.findByClienteIdUsuario(idUsuario);
    }

    @PostMapping
    public ResponseEntity<Boleta> save(@RequestBody Boleta boleta) {
        Boleta saved = boletaService.save(boleta);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Boleta> update(@PathVariable Long id, @RequestBody Boleta boleta) {
        if (!boletaService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        boleta.setIdBoleta(id);
        Boleta saved = boletaService.save(boleta);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!boletaService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        boletaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

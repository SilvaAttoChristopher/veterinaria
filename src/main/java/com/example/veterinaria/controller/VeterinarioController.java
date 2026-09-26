package com.example.veterinaria.controller;

import java.util.List;
import java.util.NoSuchElementException;

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

import com.example.veterinaria.modelo.Veterinario;
import com.example.veterinaria.service.VeterinarioService;

@RestController
@RequestMapping("/api/veterinarios")
public class VeterinarioController {

    @Autowired
    private VeterinarioService veterinarioService;

    @GetMapping
    public List<Veterinario> findAll() {
        return veterinarioService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Veterinario> findById(@PathVariable Long id) {
        return veterinarioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Veterinario> save(@RequestBody Veterinario veterinario) {
        Veterinario saved = veterinarioService.save(veterinario);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Veterinario veterinario) {
        try {
            return ResponseEntity.ok(veterinarioService.actualizarPerfil(id, veterinario));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!veterinarioService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        veterinarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

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

import com.example.veterinaria.modelo.Rol;
import com.example.veterinaria.service.RolService;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    @Autowired
    private RolService rolService;

    @GetMapping
    public List<Rol> findAll() {
        return rolService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rol> findById(@PathVariable Long id) {
        return rolService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Rol> save(@RequestBody Rol rol) {
        Rol saved = rolService.save(rol);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rol> update(@PathVariable Long id, @RequestBody Rol rol) {
        if (!rolService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        rol.setIdRol(id);
        Rol saved = rolService.save(rol);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!rolService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        rolService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

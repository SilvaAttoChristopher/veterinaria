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

import com.example.veterinaria.modelo.Vacuna;
import com.example.veterinaria.service.VacunaService;

@RestController
@RequestMapping("/api/vacunas")
public class VacunaController {

    @Autowired
    private VacunaService vacunaService;

    @GetMapping
    public List<Vacuna> findAll() {
        return vacunaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vacuna> findById(@PathVariable Long id) {
        return vacunaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Vacuna> save(@RequestBody Vacuna vacuna) {
        Vacuna saved = vacunaService.save(vacuna);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vacuna> update(@PathVariable Long id, @RequestBody Vacuna vacuna) {
        if (!vacunaService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        vacuna.setIdVacuna(id);
        Vacuna saved = vacunaService.save(vacuna);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!vacunaService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        vacunaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

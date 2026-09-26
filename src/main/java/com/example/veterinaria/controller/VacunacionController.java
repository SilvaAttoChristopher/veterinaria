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

import com.example.veterinaria.modelo.Vacunacion;
import com.example.veterinaria.service.VacunacionService;

@RestController
@RequestMapping("/api/vacunaciones")
public class VacunacionController {

    @Autowired
    private VacunacionService vacunacionService;

    @GetMapping
    public List<Vacunacion> findAll() {
        return vacunacionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vacunacion> findById(@PathVariable Long id) {
        return vacunacionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cartilla/{idCartilla}")
    public List<Vacunacion> findByCartillaIdCartilla(@PathVariable Long idCartilla) {
        return vacunacionService.findByCartillaIdCartilla(idCartilla);
    }

    @PostMapping
    public ResponseEntity<Vacunacion> save(@RequestBody Vacunacion vacunacion) {
        Vacunacion saved = vacunacionService.save(vacunacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vacunacion> update(@PathVariable Long id, @RequestBody Vacunacion vacunacion) {
        if (!vacunacionService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        vacunacion.setIdVacunacion(id);
        Vacunacion saved = vacunacionService.save(vacunacion);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!vacunacionService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        vacunacionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

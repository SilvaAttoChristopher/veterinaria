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

import com.example.veterinaria.modelo.Horario;
import com.example.veterinaria.service.HorarioService;

@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    @Autowired
    private HorarioService horarioService;

    @GetMapping
    public List<Horario> findAll() {
        return horarioService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Horario> findById(@PathVariable Long id) {
        return horarioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/veterinario/{idUsuario}")
    public List<Horario> findByUsuarioIdUsuario(@PathVariable Long idUsuario) {
        return horarioService.findByUsuarioIdUsuario(idUsuario);
    }

    @PostMapping
    public ResponseEntity<Horario> save(@RequestBody Horario horario) {
        Horario saved = horarioService.save(horario);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Horario> update(@PathVariable Long id, @RequestBody Horario horario) {
        if (!horarioService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        horario.setIdHorario(id);
        Horario saved = horarioService.save(horario);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!horarioService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        horarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

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

import com.example.veterinaria.modelo.Cartilla;
import com.example.veterinaria.service.CartillaService;

@RestController
@RequestMapping("/api/cartillas")
public class CartillaController {

    @Autowired
    private CartillaService cartillaService;

    @GetMapping
    public List<Cartilla> findAll() {
        return cartillaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cartilla> findById(@PathVariable Long id) {
        return cartillaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/mascota/{idMascota}")
    public ResponseEntity<Cartilla> findByMascotaIdMascota(@PathVariable Long idMascota) {
        return cartillaService.findByMascotaIdMascota(idMascota)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Cartilla> save(@RequestBody Cartilla cartilla) {
        Cartilla saved = cartillaService.save(cartilla);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cartilla> update(@PathVariable Long id, @RequestBody Cartilla cartilla) {
        if (!cartillaService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        cartilla.setIdCartilla(id);
        Cartilla saved = cartillaService.save(cartilla);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!cartillaService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        cartillaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

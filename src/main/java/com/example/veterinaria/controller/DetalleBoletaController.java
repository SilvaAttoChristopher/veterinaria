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

import com.example.veterinaria.modelo.DetalleBoleta;
import com.example.veterinaria.service.DetalleBoletaService;

@RestController
@RequestMapping("/api/detalle-boletas")
public class DetalleBoletaController {

    @Autowired
    private DetalleBoletaService detalleBoletaService;

    @GetMapping
    public List<DetalleBoleta> findAll() {
        return detalleBoletaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalleBoleta> findById(@PathVariable Long id) {
        return detalleBoletaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/boleta/{idBoleta}")
    public List<DetalleBoleta> findByBoletaIdBoleta(@PathVariable Long idBoleta) {
        return detalleBoletaService.findByBoletaIdBoleta(idBoleta);
    }

    @PostMapping
    public ResponseEntity<DetalleBoleta> save(@RequestBody DetalleBoleta detalleBoleta) {
        DetalleBoleta saved = detalleBoletaService.save(detalleBoleta);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetalleBoleta> update(@PathVariable Long id, @RequestBody DetalleBoleta detalleBoleta) {
        if (!detalleBoletaService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        detalleBoleta.setIdDetalleBoleta(id);
        DetalleBoleta saved = detalleBoletaService.save(detalleBoleta);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!detalleBoletaService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        detalleBoletaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

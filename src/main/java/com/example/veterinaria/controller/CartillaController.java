
package com.example.veterinaria.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.veterinaria.modelo.Cartilla;
import com.example.veterinaria.security.Autorizacion;
import com.example.veterinaria.service.CartillaPdfService;
import com.example.veterinaria.service.CartillaService;
import com.example.veterinaria.service.TitularidadService;

@RestController
@RequestMapping("/api/cartillas")
public class CartillaController {

    @Autowired
    private CartillaService cartillaService;

    @Autowired
    private TitularidadService titularidadService;

    @Autowired
    private CartillaPdfService cartillaPdfService;

    // Listar cartillas
    @GetMapping
    public List<Cartilla> findAll() {
        return cartillaService.findAll();
    }

    // Buscar cartilla por ID
    @GetMapping("/{id}")
    public ResponseEntity<Cartilla> findById(
            @PathVariable Long id) {

        return cartillaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar cartilla por mascota
    @GetMapping("/mascota/{idMascota}")
    public ResponseEntity<Cartilla> findByMascota(
            @PathVariable Long idMascota) {

        var actual = Autorizacion.actual();

        boolean permitido =
                Autorizacion.esAdmin()
                || Autorizacion.esVeterinario()
                || (actual != null
                && titularidadService.esDueño(
                        idMascota, actual.idUsuario()));

        if (!permitido) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        return cartillaService
                .findByMascotaIdMascota(idMascota)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Descargar cartilla en PDF
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarCartillaPDF(
            @PathVariable Long id) {

        var cartillaOpt = cartillaService.findById(id);

        if (cartillaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Cartilla cartilla = cartillaOpt.get();
        Long idMascota = cartilla.getMascota().getIdMascota();

        var actual = Autorizacion.actual();

        boolean permitido =
                Autorizacion.esAdmin()
                || Autorizacion.esVeterinario()
                || (actual != null
                && titularidadService.esDueño(
                        idMascota, actual.idUsuario()));

        if (!permitido) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        byte[] pdf = cartillaPdfService.generarPDF(id);

        return ResponseEntity.ok()
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"cartilla-" + id + ".pdf\""
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // Crear cartilla
    @PostMapping
    public ResponseEntity<Cartilla> save(
            @RequestBody Cartilla cartilla) {

        Cartilla saved = cartillaService.save(cartilla);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    // Actualizar cartilla
    @PutMapping("/{id}")
    public ResponseEntity<Cartilla> update(
            @PathVariable Long id,
            @RequestBody Cartilla cartilla) {

        if (cartillaService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        cartilla.setIdCartilla(id);

        Cartilla saved = cartillaService.save(cartilla);

        return ResponseEntity.ok(saved);
    }

    // Eliminar cartilla
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable Long id) {

        if (cartillaService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        cartillaService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}

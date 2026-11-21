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

import com.example.veterinaria.modelo.Mascota;
import com.example.veterinaria.security.Autorizacion;
import com.example.veterinaria.service.MascotaService;
import com.example.veterinaria.service.TitularidadService;

/**
 * Mascotas. Cada cliente sólo accede a las suyas; el veterinario y el admin
 * ven todas. El borrado es exclusivo del admin (ver WebSecurityConfig).
 */
@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    @Autowired
    private MascotaService mascotaService;

    @Autowired
    private TitularidadService titularidadService;

    @GetMapping
    public List<Mascota> findAll() {
        return mascotaService.findAll();
    }

    @GetMapping("/cliente/{idUsuario}")
    public ResponseEntity<List<Mascota>> findByUsuario(@PathVariable Long idUsuario) {
        if (!Autorizacion.esPropioOAdmin(idUsuario) && !Autorizacion.esVeterinario()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(mascotaService.findByUsuarioIdUsuario(idUsuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mascota> findById(@PathVariable Long id) {
        if (!puedeVer(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return mascotaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody Mascota mascota) {
        /* Un cliente no puede registrar una mascota a nombre de otro: se le
           fuerza su propio id como propietario. */
        if (!Autorizacion.esAdmin() && !Autorizacion.esVeterinario()) {
            var actual = Autorizacion.actual();
            if (actual == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            var cliente = new com.example.veterinaria.modelo.Usuario();
            cliente.setIdUsuario(actual.idUsuario());
            mascota.setCliente(cliente);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(mascotaService.save(mascota));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Mascota mascota) {
        /* Editar sólo el dueño o el admin: un veterinario no edita fichas ajenas
           desde aquí, y un cliente no puede tocar la mascota de otro. */
        if (!Autorizacion.esAdmin()) {
            var actual = Autorizacion.actual();
            if (actual == null || !titularidadService.esDueño(id, actual.idUsuario())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        Mascota actual = mascotaService.findById(id).orElse(null);
        if (actual == null) {
            return ResponseEntity.notFound().build();
        }

        if (mascota.getNombre() != null) {
            actual.setNombre(mascota.getNombre());
        }
        if (mascota.getEspecie() != null) {
            actual.setEspecie(mascota.getEspecie());
        }
        actual.setRaza(mascota.getRaza());
        actual.setSexo(mascota.getSexo());
        actual.setFechaNacimiento(mascota.getFechaNacimiento());
        actual.setPeso(mascota.getPeso());
        actual.setObservaciones(mascota.getObservaciones());

        return ResponseEntity.ok(mascotaService.save(actual));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!mascotaService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        mascotaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** El dueño de la mascota, o cualquier veterinario o admin, pueden verla. */
    private boolean puedeVer(Long idMascota) {
        var actual = Autorizacion.actual();
        if (Autorizacion.esAdmin() || Autorizacion.esVeterinario()) {
            return true;
        }
        return actual != null && titularidadService.esDueño(idMascota, actual.idUsuario());
    }
}

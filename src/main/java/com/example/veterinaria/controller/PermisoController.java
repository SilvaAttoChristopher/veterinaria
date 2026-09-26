package com.example.veterinaria.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.veterinaria.modelo.Permiso;
import com.example.veterinaria.modelo.Rol;
import com.example.veterinaria.modelo.RolPermiso;
import com.example.veterinaria.modelo.RolPermisoId;
import com.example.veterinaria.repository.PermisoRepository;
import com.example.veterinaria.repository.RolPermisoRepository;

/** Gestión de permisos y de su asignación a roles. Sólo ADMIN (ver WebSecurityConfig). */
@RestController
@RequestMapping("/api/permisos")
public class PermisoController {

    @Autowired
    private PermisoRepository permisoRepository;

    @Autowired
    private RolPermisoRepository rolPermisoRepository;

    @GetMapping
    public List<Permiso> findAll() {
        return permisoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Permiso> findById(@PathVariable Long id) {
        return permisoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Permiso> save(@RequestBody Permiso permiso) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permisoRepository.save(permiso));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!permisoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        permisoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rol/{idRol}")
    public List<RolPermiso> porRol(@PathVariable Long idRol) {
        return rolPermisoRepository.findByRolIdRol(idRol);
    }

    /** Asigna un permiso a un rol. */
    @PostMapping("/rol/{idRol}")
    public ResponseEntity<?> asignar(@PathVariable Long idRol, @RequestBody RolPermiso body) {
        if (body.getPermiso() == null || body.getPermiso().getIdPermiso() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Se requiere idPermiso"));
        }

        Rol rol = new Rol();
        rol.setIdRol(idRol);

        RolPermiso nuevo = new RolPermiso();
        nuevo.setRol(rol);
        nuevo.setPermiso(body.getPermiso());

        if (rolPermisoRepository.existsById(nuevo.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "El permiso ya está asignado a ese rol"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(rolPermisoRepository.save(nuevo));
    }

    @DeleteMapping("/rol/{idRol}/{idPermiso}")
    public ResponseEntity<Void> quitar(@PathVariable Long idRol, @PathVariable Long idPermiso) {
        RolPermisoId key = new RolPermisoId();
        key.setIdRol(idRol);
        key.setIdPermiso(idPermiso);

        if (!rolPermisoRepository.existsById(key)) {
            return ResponseEntity.notFound().build();
        }
        rolPermisoRepository.deleteById(key);
        return ResponseEntity.noContent().build();
    }
}

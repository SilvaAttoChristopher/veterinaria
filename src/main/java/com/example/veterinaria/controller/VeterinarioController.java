package com.example.veterinaria.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.veterinaria.modelo.Usuario;
import com.example.veterinaria.service.UsuarioService;

/**
 * Catálogo público de veterinarios para los selectores de agendar cita.
 *
 * Expone sólo los datos mínimos (id, nombre y especialidad) y únicamente de
 * veterinarios activos: no se filtran correo, teléfono, dni ni estado.
 */
@RestController
@RequestMapping("/api/veterinarios")
public class VeterinarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Map<String, Object>> listar() {
        return usuarioService.findVeterinariosActivos().stream()
                .map(this::aResumen)
                .toList();
    }

    @GetMapping("/{idUsuario}")
    public ResponseEntity<Map<String, Object>> porId(@PathVariable Long idUsuario) {
        return usuarioService.findById(idUsuario)
                .filter(u -> "VETERINARIO".equals(u.getRol().getNombre()))
                .map(this::aResumen)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> aResumen(Usuario v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("idUsuario", v.getIdUsuario());
        m.put("nombres", v.getNombres());
        m.put("apellidos", v.getApellidos());
        m.put("especialidad", v.getEspecialidad());
        return m;
    }
}

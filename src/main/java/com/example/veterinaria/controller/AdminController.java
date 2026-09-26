package com.example.veterinaria.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.veterinaria.modelo.Permiso;
import com.example.veterinaria.repository.BoletaRepository;
import com.example.veterinaria.repository.CitaRepository;
import com.example.veterinaria.repository.HistorialRepository;
import com.example.veterinaria.repository.MascotaRepository;
import com.example.veterinaria.repository.PermisoRepository;
import com.example.veterinaria.repository.RolRepository;
import com.example.veterinaria.repository.ServicioRepository;
import com.example.veterinaria.repository.UsuarioRepository;
import com.example.veterinaria.service.UsuarioService;

/** Endpoints de administración: resumen estadístico y catálogo de permisos. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MascotaRepository mascotaRepository;
    @Autowired private CitaRepository citaRepository;
    @Autowired private HistorialRepository historialRepository;
    @Autowired private BoletaRepository boletaRepository;
    @Autowired private ServicioRepository servicioRepository;
    @Autowired private PermisoRepository permisoRepository;
    @Autowired private RolRepository rolRepository;

    /** Indicadores para el panel de resumen. */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("totalUsuarios", usuarioRepository.count());
        stats.put("totalClientes", usuarioService.findByRolNombre("CLIENTE").size());
        stats.put("totalVeterinarios", usuarioService.findByRolNombre("VETERINARIO").size());
        stats.put("totalMascotas", mascotaRepository.count());
        stats.put("totalCitas", citaRepository.count());
        stats.put("totalHistoriales", historialRepository.count());
        stats.put("totalBoletas", boletaRepository.count());
        stats.put("totalServicios", servicioRepository.count());

        List<com.example.veterinaria.modelo.Cita> citas = citaRepository.findAll();
        stats.put("citasProgramadas", contarPorEstado(citas, "PROGRAMADA"));
        stats.put("citasAtendidas", contarPorEstado(citas, "ATENDIDA"));
        stats.put("citasCanceladas", contarPorEstado(citas, "CANCELADA"));

        return ResponseEntity.ok(stats);
    }

    private long contarPorEstado(List<com.example.veterinaria.modelo.Cita> citas, String estado) {
        return citas.stream().filter(c -> estado.equals(c.getEstado())).count();
    }

    @GetMapping("/permisos")
    public List<Permiso> listarPermisos() {
        return permisoRepository.findAll();
    }

    @GetMapping("/roles")
    public List<?> listarRoles() {
        return rolRepository.findAll();
    }
}

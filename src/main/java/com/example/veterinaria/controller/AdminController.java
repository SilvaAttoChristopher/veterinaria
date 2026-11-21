package com.example.veterinaria.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.veterinaria.modelo.Cita;
import com.example.veterinaria.modelo.Permiso;
import com.example.veterinaria.repository.BoletaRepository;
import com.example.veterinaria.repository.CitaRepository;
import com.example.veterinaria.repository.HistorialRepository;
import com.example.veterinaria.repository.MascotaRepository;
import com.example.veterinaria.repository.PermisoRepository;
import com.example.veterinaria.repository.RolRepository;
import com.example.veterinaria.repository.ServicioRepository;
import com.example.veterinaria.repository.UsuarioRepository;
import com.example.veterinaria.service.CitaService;
import com.example.veterinaria.service.UsuarioService;

/** Endpoints de administración: resumen estadístico y catálogo de permisos. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MascotaRepository mascotaRepository;
    @Autowired private CitaRepository citaRepository;
    @Autowired private CitaService citaService;
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

        List<Cita> citas = citaRepository.findAll();
        stats.put("citasProgramadas", contarPorEstado(citas, "PROGRAMADA"));
        stats.put("citasAtendidas", contarPorEstado(citas, "ATENDIDA"));
        stats.put("citasCanceladas", contarPorEstado(citas, "CANCELADA"));

        return ResponseEntity.ok(stats);
    }

    /**
     * Reporte de citas: listado completo con mascota, dueño, veterinario y
     * servicio. Todos los filtros son opcionales; los que no llegan no restringen
     * el resultado.
     */
    @GetMapping("/citas")
    public ResponseEntity<List<Cita>> reporteCitas(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long veterinario,
            @RequestParam(required = false) Long mascota,
            @RequestParam(required = false) Long servicio) {

        return ResponseEntity.ok(citaService.buscarParaReporte(
                desde, hasta, estado, veterinario, mascota, servicio));
    }

    private long contarPorEstado(List<Cita> citas, String estado) {
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

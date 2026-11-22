package com.example.veterinaria.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.veterinaria.modelo.Usuario;
import com.example.veterinaria.service.PagoService;
import com.example.veterinaria.service.UsuarioService;

@Controller
public class PageController {

    /**
     * Rol del usuario autenticado, disponible en todas las vistas para elegir la
     * barra lateral correcta (cliente, veterinario o administración).
     */
    @ModelAttribute("rolActual")
    public String rolActual(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .findFirst()
                .orElse(null);
    }

    /** Nombre del usuario autenticado, para el encabezado de la barra lateral. */
    @ModelAttribute("usuarioActual")
    public String usuarioActual(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PagoService pagoService;

    // ---------------------------------------------------------------- pagos

    /** Retorno de Stripe tras pagar: confirma el pago y muestra el resultado. */
    @GetMapping("/pago/exito")
    public String pagoExito(
            @RequestParam(name = "session_id", required = false) String sessionId, Model model) {
        model.addAttribute("resultado", pagoService.procesarRetorno(sessionId));
        return conActiva(model, "citas", "pago/exito");
    }

    /** El cliente abandonó el pago en Stripe: se libera la reserva. */
    @GetMapping("/pago/cancelado")
    public String pagoCancelado(
            @RequestParam(name = "session_id", required = false) String sessionId, Model model) {
        if (sessionId != null && !sessionId.isBlank()) {
            pagoService.cancelarPorSesion(sessionId);
        }
        model.addAttribute("sessionId", sessionId);
        return conActiva(model, "citas", "pago/cancelado");
    }

@ModelAttribute("fotoPerfilUrl")
public String fotoPerfilUrl(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
        return null;
    }

    Usuario usuario = usuarioService.findByUsername(authentication.getName()).orElse(null);
    if (usuario == null || usuario.getFotoPerfil() == null) {
        return null;
    }

    return "/api/perfil/foto?v=" + usuario.getFotoPerfil();
}

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/staff")
    public String staff() {
        return "staff";
    }

    // ---------------------------------------------------------------- cliente

    @GetMapping("/dashboard/perfil")
    public String dashboardPerfil(Model model) {
        return conActiva(model, "perfil", "dashboard/perfil");
    }

    @GetMapping("/dashboard/editar-perfil")
    public String editarPerfil(Model model) {
        return conActiva(model, "perfil", "dashboard/editar-perfil");
    }

    @GetMapping("/dashboard/cambiar-password")
    public String cambiarPassword(Model model) {
        return conActiva(model, "password", "dashboard/cambiar-password");
    }

    @GetMapping("/dashboard/mascotas")
    public String dashboardMascotas(Model model) {
        return conActiva(model, "mascotas", "dashboard/mascotas");
    }

    @GetMapping("/dashboard/registrar-mascota")
    public String registrarMascota(Model model) {
        model.addAttribute("idMascota", null);
        return conActiva(model, "mascotas", "dashboard/mascota-form");
    }

    @GetMapping("/dashboard/editar-mascota/{id}")
    public String editarMascota(@PathVariable Long id, Model model) {
        model.addAttribute("idMascota", id);
        return conActiva(model, "mascotas", "dashboard/mascota-form");
    }

    @GetMapping("/dashboard/mascota/{id}")
    public String detalleMascota(@PathVariable Long id, Model model) {
        model.addAttribute("idMascota", id);
        return conActiva(model, "mascotas", "dashboard/mascota-detalle");
    }

    @GetMapping("/dashboard/citas")
    public String dashboardCitas(Model model) {
        return conActiva(model, "citas", "dashboard/citas");
    }

    @GetMapping("/dashboard/registrar-cita")
    public String registrarCita(Model model) {
        model.addAttribute("idCita", null);
        return conActiva(model, "citas", "dashboard/cita-form");
    }

    @GetMapping("/dashboard/editar-cita/{id}")
    public String editarCita(@PathVariable Long id, Model model) {
        model.addAttribute("idCita", id);
        return conActiva(model, "citas", "dashboard/cita-form");
    }

    @GetMapping("/dashboard/cartilla")
    public String dashboardCartilla(Model model) {
        return conActiva(model, "cartilla", "dashboard/cartilla");
    }

    @GetMapping("/dashboard/registrar-vacunacion")
    public String registrarVacunacion(Model model) {
        return conActiva(model, "cartilla", "dashboard/vacunacion-form");
    }

    @GetMapping("/dashboard/notificaciones")
    public String dashboardNotificaciones(Model model) {
        return conActiva(model, "notificaciones", "dashboard/notificaciones");
    }

    // ------------------------------------------------------------ veterinario

    @GetMapping("/dashboard/perfil-medico")
    public String dashboardPerfilMedico(Model model) {
        return conActiva(model, "perfil", "dashboard/perfil-medico");
    }

    @GetMapping("/dashboard/editar-perfil-medico")
    public String editarPerfilMedico(Model model) {
        return conActiva(model, "perfil", "dashboard/editar-perfil-medico");
    }

    @GetMapping("/dashboard/registrar-horario")
    public String registrarHorario(Model model) {
        model.addAttribute("idHorario", null);
        return conActiva(model, "horarios", "dashboard/horario-form");
    }

    @GetMapping("/dashboard/editar-horario/{id}")
    public String editarHorario(@PathVariable Long id, Model model) {
        model.addAttribute("idHorario", id);
        return conActiva(model, "horarios", "dashboard/horario-form");
    }

    @GetMapping("/dashboard/mascotas-vet")
    public String dashboardMascotasVet(Model model) {
        return conActiva(model, "mascotas", "dashboard/mascotas-vet");
    }

    
    @GetMapping("/dashboard/mascota-vet/{id}")
    public String consultarMascotaVet(@PathVariable Long id, Model model) {
        model.addAttribute("idMascota", id);
        return conActiva(model, "mascotas", "dashboard/mascota-consulta-vet");
    }


    @GetMapping("/dashboard/citas-vet")
    public String dashboardCitasVet(Model model) {
        return conActiva(model, "citas", "dashboard/citas-vet");
    }

    @GetMapping("/dashboard/registrar-atencion/{idCita}")
    public String registrarAtencion(@PathVariable Long idCita, Model model) {
        model.addAttribute("idCita", idCita);
        return conActiva(model, "citas", "dashboard/atencion-form");
    }

    // ----------------------------------------------------------------- admin

    @GetMapping("/dashboard/admin")
    public String adminIndice(Model model) {
        return conActiva(model, "resumen", "dashboard/admin/indice");
    }

    @GetMapping("/dashboard/admin/usuarios")
    public String adminUsuarios(Model model) {
        return conActiva(model, "usuarios", "dashboard/admin/usuarios");
    }

    @GetMapping("/dashboard/admin/registrar-usuario")
    public String adminRegistrarUsuario(Model model) {
        return conActiva(model, "usuarios", "dashboard/admin/usuario-form");
    }

    @GetMapping("/dashboard/admin/editar-usuario/{id}")
    public String adminEditarUsuario(@PathVariable Long id, Model model) {
        model.addAttribute("idUsuario", id);
        return conActiva(model, "usuarios", "dashboard/admin/editar-usuario");
    }

    @GetMapping("/dashboard/admin/veterinarios")
    public String adminVeterinarios(Model model) {
        return conActiva(model, "veterinarios", "dashboard/admin/veterinarios");
    }

    @GetMapping("/dashboard/admin/servicios")
    public String adminServicios(Model model) {
        return conActiva(model, "servicios", "dashboard/admin/servicios");
    }

    @GetMapping("/dashboard/admin/registrar-servicio")
    public String adminRegistrarServicio(Model model) {
        model.addAttribute("idServicio", null);
        return conActiva(model, "servicios", "dashboard/admin/servicio-form");
    }

    @GetMapping("/dashboard/admin/editar-servicio/{id}")
    public String adminEditarServicio(@PathVariable Long id, Model model) {
        model.addAttribute("idServicio", id);
        return conActiva(model, "servicios", "dashboard/admin/servicio-form");
    }

    @GetMapping("/dashboard/admin/roles")
    public String adminRoles(Model model) {
        return conActiva(model, "roles", "dashboard/admin/roles");
    }

    @GetMapping("/dashboard/admin/boletas")
    public String adminBoletas(Model model) {
        return conActiva(model, "boletas", "dashboard/admin/boletas");
    }

    @GetMapping("/dashboard/admin/citas")
    public String adminCitas(Model model) {
        return conActiva(model, "citas", "dashboard/admin/citas");
    }

    @GetMapping("/dashboard/admin/historial")
    public String adminHistorial(Model model) {
        return conActiva(model, "historial", "dashboard/admin/historial");
    }

    private String conActiva(Model model, String activePage, String vista) {
        model.addAttribute("activePage", activePage);
        return vista;
    }
}

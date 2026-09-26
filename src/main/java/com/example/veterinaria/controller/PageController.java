package com.example.veterinaria.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

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
    public String dashboardPerfil() {
        return "dashboard/perfil";
    }

    @GetMapping("/dashboard/editar-perfil")
    public String editarPerfil() {
        return "dashboard/editar-perfil";
    }

    @GetMapping("/dashboard/cambiar-password")
    public String cambiarPassword() {
        return "dashboard/cambiar-password";
    }

    @GetMapping("/dashboard/mascotas")
    public String dashboardMascotas() {
        return "dashboard/mascotas";
    }

    @GetMapping("/dashboard/registrar-mascota")
    public String registrarMascota(Model model) {
        model.addAttribute("idMascota", null);
        return "dashboard/mascota-form";
    }

    @GetMapping("/dashboard/editar-mascota/{id}")
    public String editarMascota(@PathVariable Long id, Model model) {
        model.addAttribute("idMascota", id);
        return "dashboard/mascota-form";
    }

    @GetMapping("/dashboard/mascota/{id}")
    public String detalleMascota(@PathVariable Long id, Model model) {
        model.addAttribute("idMascota", id);
        return "dashboard/mascota-detalle";
    }

    @GetMapping("/dashboard/citas")
    public String dashboardCitas() {
        return "dashboard/citas";
    }

    @GetMapping("/dashboard/registrar-cita")
    public String registrarCita(Model model) {
        model.addAttribute("idCita", null);
        return "dashboard/cita-form";
    }

    @GetMapping("/dashboard/editar-cita/{id}")
    public String editarCita(@PathVariable Long id, Model model) {
        model.addAttribute("idCita", id);
        return "dashboard/cita-form";
    }

    @GetMapping("/dashboard/cartilla")
    public String dashboardCartilla() {
        return "dashboard/cartilla";
    }

    @GetMapping("/dashboard/registrar-vacunacion")
    public String registrarVacunacion() {
        return "dashboard/vacunacion-form";
    }

    @GetMapping("/dashboard/notificaciones")
    public String dashboardNotificaciones() {
        return "dashboard/notificaciones";
    }

    // ------------------------------------------------------------ veterinario

    @GetMapping("/dashboard/perfil-medico")
    public String dashboardPerfilMedico() {
        return "dashboard/perfil-medico";
    }

    @GetMapping("/dashboard/editar-perfil-medico")
    public String editarPerfilMedico() {
        return "dashboard/editar-perfil-medico";
    }

    @GetMapping("/dashboard/registrar-horario")
    public String registrarHorario(Model model) {
        model.addAttribute("idHorario", null);
        return "dashboard/horario-form";
    }

    @GetMapping("/dashboard/editar-horario/{id}")
    public String editarHorario(@PathVariable Long id, Model model) {
        model.addAttribute("idHorario", id);
        return "dashboard/horario-form";
    }

    @GetMapping("/dashboard/mascotas-vet")
    public String dashboardMascotasVet() {
        return "dashboard/mascotas-vet";
    }

    @GetMapping("/dashboard/citas-vet")
    public String dashboardCitasVet() {
        return "dashboard/citas-vet";
    }

    @GetMapping("/dashboard/registrar-atencion/{idCita}")
    public String registrarAtencion(@PathVariable Long idCita, Model model) {
        model.addAttribute("idCita", idCita);
        return "dashboard/atencion-form";
    }
}

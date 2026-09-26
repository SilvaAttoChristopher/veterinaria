package com.example.veterinaria.config;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.veterinaria.modelo.*;
import com.example.veterinaria.repository.*;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private RolRepository rolRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MascotaRepository mascotaRepository;
    @Autowired private ServicioRepository servicioRepository;
    @Autowired private VacunaRepository vacunaRepository;
    @Autowired private HorarioRepository horarioRepository;
    @Autowired private PermisoRepository permisoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        /* Se comprueba por el usuario semilla y no por count(): si una siembra
           anterior quedó a medias, así se puede reparar en el siguiente arranque. */
        if (usuarioRepository.existsByUsername("admin@vet.com")) {
            return;
        }

        // Roles
        Rol rolCliente = saveRol("CLIENTE");
        Rol rolVet = saveRol("VETERINARIO");
        Rol rolAdmin = saveRol("ADMIN");

        // Permisos
        String[][] permisos = {
            {"VER_PERFIL", "Ver perfil propio"},
            {"EDITAR_PERFIL", "Editar perfil propio"},
            {"GESTIONAR_MASCOTAS", "Gestionar mascotas propias"},
            {"GESTIONAR_CITAS", "Gestionar citas propias"},
            {"VER_HISTORIAL", "Ver historial medico"},
            {"GESTIONAR_HORARIOS", "Gestionar horarios"},
            {"ATENDER_CITAS", "Atender citas"},
            {"GESTIONAR_SERVICIOS", "Gestionar servicios"}
        };
        for (String[] p : permisos) {
            Permiso permiso = new Permiso();
            permiso.setNombre(p[0]);
            permiso.setDescripcion(p[1]);
            permisoRepository.save(permiso);
        }

        // Admin (plain Usuario, not Cliente/Vet)
        Usuario admin = new Usuario();
        admin.setUsername("admin@vet.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNombres("Administrador");
        admin.setApellidos("General");
        admin.setTelefono("999999999");
        admin.setCorreo("admin@vet.com");
        admin.setRol(rolAdmin);
        admin.setEstado("ACTIVO");
        usuarioRepository.save(admin);

        // Cliente (tabla única: mismo Usuario, con dni y rol CLIENTE)
        Usuario cliente = new Usuario();
        cliente.setUsername("cliente@test.com");
        cliente.setPassword(passwordEncoder.encode("cliente123"));
        cliente.setNombres("Juan");
        cliente.setApellidos("Perez");
        cliente.setTelefono("900111222");
        cliente.setCorreo("cliente@test.com");
        cliente.setDireccion("Av. Venezuela 30, Lima");
        cliente.setRol(rolCliente);
        cliente.setDni("12345678");
        cliente.setEstado("ACTIVO");
        Usuario savedCliente = usuarioRepository.save(cliente);

        // Veterinario (mismo Usuario, con especialidad y rol VETERINARIO)
        Usuario vet = new Usuario();
        vet.setUsername("vet@test.com");
        vet.setPassword(passwordEncoder.encode("vet123"));
        vet.setNombres("Dra. Maria");
        vet.setApellidos("Garcia");
        vet.setTelefono("900333444");
        vet.setCorreo("vet@test.com");
        vet.setRol(rolVet);
        vet.setEspecialidad("Traumatologia y Neurologia");
        vet.setEstado("ACTIVO");
        Usuario savedVet = usuarioRepository.save(vet);

        // Mascotas
        Mascota max = new Mascota();
        max.setCliente(savedCliente);
        max.setNombre("Max");
        max.setEspecie("Perro");
        max.setRaza("Golden Retriever");
        max.setSexo('M');
        mascotaRepository.save(max);

        Mascota luna = new Mascota();
        luna.setCliente(savedCliente);
        luna.setNombre("Luna");
        luna.setEspecie("Gato");
        luna.setRaza("Siames");
        luna.setSexo('F');
        mascotaRepository.save(luna);

        // Servicios
        String[][] servicios = {
            {"Consulta General", "Consulta veterinaria general", "50"},
            {"Consulta Especializada", "Consulta con especialista", "80"},
            {"Vacunacion", "Aplicacion de vacuna", "30"},
            {"Cirugia", "Procedimiento quirurgico", "200"},
            {"Radiografia", "Estudio de imagen por rayos X", "60"}
        };
        for (String[] s : servicios) {
            Servicio servicio = new Servicio();
            servicio.setNombre(s[0]);
            servicio.setDescripcion(s[1]);
            servicio.setPrecio(java.math.BigDecimal.valueOf(Double.parseDouble(s[2])));
            servicioRepository.save(servicio);
        }

        // Vacunas (catálogo necesario para la cartilla de vacunación)
        String[][] vacunas = {
            {"Rabia", "Vacuna antirrábica anual"},
            {"Parvovirus", "Prevención de parvovirosis"},
            {"Desparasitante", "Tratamiento antiparasitario"},
            {"Polivalente", "Vacuna polivalente canina y felina"}
        };
        for (String[] v : vacunas) {
            Vacuna vacuna = new Vacuna();
            vacuna.setNombre(v[0]);
            vacuna.setDescripcion(v[1]);
            vacuna.setVigente(true);
            vacunaRepository.save(vacuna);
        }

        // Horarios (Lunes a Viernes 8:00 - 17:00)
        for (int dia = 1; dia <= 5; dia++) {
            Horario horario = new Horario();
            horario.setVeterinario(savedVet);
            horario.setDiaSemana((short) dia);
            horario.setHoraInicio(java.time.LocalTime.of(8, 0));
            horario.setHoraFin(java.time.LocalTime.of(17, 0));
            horario.setActivo(true);
            horarioRepository.save(horario);
        }

        System.out.println("=== SEED DATA CREATED ===");
        System.out.println("Admin:     admin@vet.com / admin123");
        System.out.println("Cliente:   cliente@test.com / cliente123");
        System.out.println("Veterinario: vet@test.com / vet123");
    }

    private Rol saveRol(String nombre) {
        Optional<Rol> existing = rolRepository.findByNombre(nombre);
        if (existing.isPresent()) return existing.get();
        Rol rol = new Rol();
        rol.setNombre(nombre);
        return rolRepository.save(rol);
    }
}

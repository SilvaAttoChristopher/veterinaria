package com.example.veterinaria.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.veterinaria.modelo.Cita;
import com.example.veterinaria.modelo.Mascota;
import com.example.veterinaria.modelo.Notificacion;
import com.example.veterinaria.repository.CitaRepository;
import com.example.veterinaria.repository.MascotaRepository;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private HorarioService horarioService;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private MascotaRepository mascotaRepository;

    public List<Cita> findAll() {
        return citaRepository.findAll();
    }

    public Optional<Cita> findById(Long id) {
        return citaRepository.findById(id);
    }

    public List<Cita> findByMascotaIdMascota(Long idMascota) {
        return citaRepository.findByMascotaIdMascota(idMascota);
    }

    public List<Cita> findByUsuarioIdUsuarioAndFecha(Long idUsuario, LocalDate fecha) {
        return citaRepository.findByVeterinarioIdUsuarioAndFecha(idUsuario, fecha);
    }

    public List<Cita> findByEstado(String estado) {
        return citaRepository.findByEstado(estado);
    }

    public boolean verificarDisponibilidad(Long idVeterinario, LocalDate fecha, LocalTime hora) {
        Short diaSemana = (short) fecha.getDayOfWeek().getValue();
        List<com.example.veterinaria.modelo.Horario> horarios = horarioService.findByUsuarioIdUsuarioAndDiaSemana(idVeterinario, diaSemana);
        
        boolean tieneHorario = horarios.stream()
            .anyMatch(h -> h.getActivo() && 
                           !hora.isBefore(h.getHoraInicio()) && 
                           hora.isBefore(h.getHoraFin()));
        
        if (!tieneHorario) {
            return false;
        }

        List<Cita> citasExistentes = findByUsuarioIdUsuarioAndFecha(idVeterinario, fecha);
        boolean horaOcupada = citasExistentes.stream()
            .anyMatch(c -> c.getHora().equals(hora) && 
                          !"CANCELADA".equals(c.getEstado()));
        
        return !horaOcupada;
    }

    /**
     * Guarda una cita. Al crearla se comprueba la disponibilidad del veterinario y se
     * notifica al dueño de la mascota.
     *
     * La cita y su notificación se escriben en la misma transacción para que no quede
     * una cita agendada sin aviso si algo falla.
     */
    @Transactional
    public Cita save(Cita cita) {
        if (cita.getIdCita() != null) {
            return citaRepository.save(cita);
        }

        if (cita.getMascota() == null || cita.getMascota().getIdMascota() == null) {
            throw new IllegalArgumentException("La cita debe indicar la mascota");
        }

        if (cita.getVeterinario() == null || cita.getVeterinario().getIdUsuario() == null) {
            throw new IllegalArgumentException("La cita debe indicar el veterinario");
        }

        if (!verificarDisponibilidad(
                cita.getVeterinario().getIdUsuario(),
                cita.getFecha(),
                cita.getHora())) {
            throw new RuntimeException("El horario no está disponible para este veterinario");
        }

        /* Se carga la mascota desde la base de datos: el cliente envía sólo el
           idMascota, por lo que la referencia anidada viene sin su propietario. */
        Mascota mascota = mascotaRepository.findById(cita.getMascota().getIdMascota())
                .orElseThrow(() -> new IllegalArgumentException("La mascota no existe"));

        Cita saved = citaRepository.save(cita);

        if (mascota.getCliente() != null) {
            Notificacion notificacion = new Notificacion();
            notificacion.setUsuario(mascota.getCliente());
            notificacion.setTipo("CITA_PROXIMA");
            notificacion.setMensaje(String.format(
                    "Su cita para %s esta programada para el %s a las %s",
                    mascota.getNombre(),
                    cita.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    cita.getHora()));
            notificacionService.save(notificacion);
        }

        return saved;
    }

    public void deleteById(Long id) {
        citaRepository.deleteById(id);
    }
}

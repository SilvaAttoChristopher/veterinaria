package com.example.veterinaria.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.veterinaria.modelo.Cita;
import com.example.veterinaria.modelo.Notificacion;
import com.example.veterinaria.repository.CitaRepository;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private HorarioService horarioService;

    @Autowired
    private NotificacionService notificacionService;

    public List<Cita> findAll() {
        return citaRepository.findAll();
    }

    public Optional<Cita> findById(Long id) {
        return citaRepository.findById(id);
    }

    public List<Cita> findByMascotaIdMascota(Long idMascota) {
        return citaRepository.findByMascotaIdMascota(idMascota);
    }

    public List<Cita> findByVeterinarioIdUsuarioAndFecha(Long idUsuario, LocalDate fecha) {
        return citaRepository.findByVeterinarioIdUsuarioAndFecha(idUsuario, fecha);
    }

    public List<Cita> findByEstado(String estado) {
        return citaRepository.findByEstado(estado);
    }

    public boolean verificarDisponibilidad(Long idVeterinario, LocalDate fecha, LocalTime hora) {
        Short diaSemana = (short) fecha.getDayOfWeek().getValue();
        List<com.example.veterinaria.modelo.Horario> horarios = horarioService.findByVeterinarioIdUsuarioAndDiaSemana(idVeterinario, diaSemana);
        
        boolean tieneHorario = horarios.stream()
            .anyMatch(h -> h.getActivo() && 
                           !hora.isBefore(h.getHoraInicio()) && 
                           hora.isBefore(h.getHoraFin()));
        
        if (!tieneHorario) {
            return false;
        }

        List<Cita> citasExistentes = findByVeterinarioIdUsuarioAndFecha(idVeterinario, fecha);
        boolean horaOcupada = citasExistentes.stream()
            .anyMatch(c -> c.getHora().equals(hora) && 
                          !"CANCELADA".equals(c.getEstado()));
        
        return !horaOcupada;
    }

    public Cita save(Cita cita) {
        if (cita.getIdCita() == null) {
            if (!verificarDisponibilidad(
                    cita.getVeterinario().getIdUsuario(), 
                    cita.getFecha(), 
                    cita.getHora())) {
                throw new RuntimeException("El horario no está disponible para este veterinario");
            }
            
            Cita saved = citaRepository.save(cita);
            
            // Create notification for the client
            Notificacion notificacion = new Notificacion();
            notificacion.setUsuario(cita.getMascota().getCliente());
            notificacion.setTipo("CITA_PROXIMA");
            notificacion.setMensaje(String.format(
                "Su cita para %s está programada para el %s a las %s",
                cita.getMascota().getNombre(),
                cita.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                cita.getHora()
            ));
            notificacionService.save(notificacion);
            
            return saved;
        }
        return citaRepository.save(cita);
    }

    public void deleteById(Long id) {
        citaRepository.deleteById(id);
    }
}

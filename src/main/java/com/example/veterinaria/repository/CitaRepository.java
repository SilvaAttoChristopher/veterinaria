package com.example.veterinaria.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.veterinaria.modelo.Cita;

/** JpaSpecificationExecutor permite armar el reporte de citas con filtros opcionales. */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long>, JpaSpecificationExecutor<Cita> {

    List<Cita> findByMascotaIdMascota(Long idMascota);

    List<Cita> findByVeterinarioIdUsuario(Long idUsuario);

    List<Cita> findByMascotaClienteIdUsuario(Long idUsuario);

    List<Cita> findByVeterinarioIdUsuarioAndFecha(Long idUsuario, LocalDate fecha);

    List<Cita> findByEstado(String estado);

    List<Cita> findByVeterinarioIdUsuarioAndFechaAndHoraAndEstado(
            Long idUsuario, LocalDate fecha, LocalTime hora, String estado);
}

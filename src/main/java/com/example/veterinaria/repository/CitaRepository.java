package com.example.veterinaria.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.veterinaria.modelo.Cita;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByMascotaIdMascota(Long idMascota);

    List<Cita> findByVeterinarioIdUsuarioAndFecha(Long idUsuario, LocalDate fecha);

    List<Cita> findByEstado(String estado);
}

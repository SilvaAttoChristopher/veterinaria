package com.example.veterinaria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.veterinaria.modelo.Historial;

@Repository
public interface HistorialRepository extends JpaRepository<Historial, Long> {

    List<Historial> findByMascotaIdMascotaOrderByFechaAtencionDesc(Long idMascota);

    List<Historial> findByVeterinarioIdUsuario(Long idUsuario);
}

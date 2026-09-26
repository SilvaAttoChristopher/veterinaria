package com.example.veterinaria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.veterinaria.modelo.Vacunacion;

@Repository
public interface VacunacionRepository extends JpaRepository<Vacunacion, Long> {

    List<Vacunacion> findByCartillaIdCartilla(Long idCartilla);
}

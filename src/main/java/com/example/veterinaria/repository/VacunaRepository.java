package com.example.veterinaria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.veterinaria.modelo.Vacuna;

@Repository
public interface VacunaRepository extends JpaRepository<Vacuna, Long> {

    Optional<Vacuna> findByNombre(String nombre);
}

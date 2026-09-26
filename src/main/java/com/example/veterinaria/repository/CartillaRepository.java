package com.example.veterinaria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.veterinaria.modelo.Cartilla;

@Repository
public interface CartillaRepository extends JpaRepository<Cartilla, Long> {

    Optional<Cartilla> findByMascotaIdMascota(Long idMascota);
}

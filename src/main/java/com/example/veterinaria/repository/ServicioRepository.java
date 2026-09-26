package com.example.veterinaria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.veterinaria.modelo.Servicio;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findByActivoTrue();
}

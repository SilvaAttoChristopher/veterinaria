package com.example.veterinaria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.veterinaria.modelo.DetalleBoleta;

@Repository
public interface DetalleBoletaRepository extends JpaRepository<DetalleBoleta, Long> {

    List<DetalleBoleta> findByBoletaIdBoleta(Long idBoleta);
}

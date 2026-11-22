package com.example.veterinaria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.veterinaria.modelo.Pago;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByStripeSessionId(String stripeSessionId);

    /** Último pago registrado para una cita (se reutiliza al reintentar el pago). */
    Optional<Pago> findFirstByCitaIdCitaOrderByFechaCreacionDesc(Long idCita);

    List<Pago> findByCitaIdCita(Long idCita);
}

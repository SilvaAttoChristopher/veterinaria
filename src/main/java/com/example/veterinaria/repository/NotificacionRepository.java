package com.example.veterinaria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.veterinaria.modelo.Notificacion;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioIdUsuarioAndEstadoLeidaFalse(Long idUsuario);

    List<Notificacion> findByUsuarioIdUsuario(Long idUsuario);
}

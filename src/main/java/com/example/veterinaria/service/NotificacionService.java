package com.example.veterinaria.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.veterinaria.modelo.Notificacion;
import com.example.veterinaria.repository.NotificacionRepository;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    public List<Notificacion> findAll() {
        return notificacionRepository.findAll();
    }

    public Optional<Notificacion> findById(Long id) {
        return notificacionRepository.findById(id);
    }

    public List<Notificacion> findByUsuarioIdUsuarioAndEstadoLeidaFalse(Long idUsuario) {
        return notificacionRepository.findByUsuarioIdUsuarioAndEstadoLeidaFalse(idUsuario);
    }

    public List<Notificacion> findByUsuarioIdUsuario(Long idUsuario) {
        return notificacionRepository.findByUsuarioIdUsuario(idUsuario);
    }

    public Notificacion save(Notificacion notificacion) {
        return notificacionRepository.save(notificacion);
    }

    public void deleteById(Long id) {
        notificacionRepository.deleteById(id);
    }
}

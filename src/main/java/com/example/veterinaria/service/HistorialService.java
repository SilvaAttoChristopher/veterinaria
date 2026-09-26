package com.example.veterinaria.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.veterinaria.modelo.Historial;
import com.example.veterinaria.repository.HistorialRepository;

@Service
public class HistorialService {

    @Autowired
    private HistorialRepository historialRepository;

    public List<Historial> findAll() {
        return historialRepository.findAll();
    }

    public Optional<Historial> findById(Long id) {
        return historialRepository.findById(id);
    }

    public List<Historial> findByMascotaIdMascotaOrderByFechaAtencionDesc(Long idMascota) {
        return historialRepository.findByMascotaIdMascotaOrderByFechaAtencionDesc(idMascota);
    }

    public List<Historial> findByUsuarioIdUsuario(Long idUsuario) {
        return historialRepository.findByVeterinarioIdUsuario(idUsuario);
    }

    public Historial save(Historial historial) {
        return historialRepository.save(historial);
    }

    public void deleteById(Long id) {
        historialRepository.deleteById(id);
    }
}

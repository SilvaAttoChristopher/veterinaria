package com.example.veterinaria.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.veterinaria.modelo.DetalleBoleta;
import com.example.veterinaria.repository.DetalleBoletaRepository;

@Service
public class DetalleBoletaService {

    @Autowired
    private DetalleBoletaRepository detalleBoletaRepository;

    public List<DetalleBoleta> findAll() {
        return detalleBoletaRepository.findAll();
    }

    public Optional<DetalleBoleta> findById(Long id) {
        return detalleBoletaRepository.findById(id);
    }

    public List<DetalleBoleta> findByBoletaIdBoleta(Long idBoleta) {
        return detalleBoletaRepository.findByBoletaIdBoleta(idBoleta);
    }

    public DetalleBoleta save(DetalleBoleta detalleBoleta) {
        return detalleBoletaRepository.save(detalleBoleta);
    }

    public void deleteById(Long id) {
        detalleBoletaRepository.deleteById(id);
    }
}

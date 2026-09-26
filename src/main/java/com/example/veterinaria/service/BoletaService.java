package com.example.veterinaria.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.veterinaria.modelo.Boleta;
import com.example.veterinaria.repository.BoletaRepository;

@Service
public class BoletaService {

    @Autowired
    private BoletaRepository boletaRepository;

    public List<Boleta> findAll() {
        return boletaRepository.findAll();
    }

    public Optional<Boleta> findById(Long id) {
        return boletaRepository.findById(id);
    }

    public List<Boleta> findByClienteIdUsuario(Long idUsuario) {
        return boletaRepository.findByClienteIdUsuario(idUsuario);
    }

    public Boleta save(Boleta boleta) {
        return boletaRepository.save(boleta);
    }

    public void deleteById(Long id) {
        boletaRepository.deleteById(id);
    }
}

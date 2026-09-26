package com.example.veterinaria.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.veterinaria.modelo.Vacunacion;
import com.example.veterinaria.repository.VacunacionRepository;

@Service
public class VacunacionService {

    @Autowired
    private VacunacionRepository vacunacionRepository;

    public List<Vacunacion> findAll() {
        return vacunacionRepository.findAll();
    }

    public Optional<Vacunacion> findById(Long id) {
        return vacunacionRepository.findById(id);
    }

    public List<Vacunacion> findByCartillaIdCartilla(Long idCartilla) {
        return vacunacionRepository.findByCartillaIdCartilla(idCartilla);
    }

    public Vacunacion save(Vacunacion vacunacion) {
        return vacunacionRepository.save(vacunacion);
    }

    public void deleteById(Long id) {
        vacunacionRepository.deleteById(id);
    }
}

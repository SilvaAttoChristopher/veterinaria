package com.example.veterinaria.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.veterinaria.modelo.Vacuna;
import com.example.veterinaria.repository.VacunaRepository;

@Service
public class VacunaService {

    @Autowired
    private VacunaRepository vacunaRepository;

    public List<Vacuna> findAll() {
        return vacunaRepository.findAll();
    }

    public Optional<Vacuna> findById(Long id) {
        return vacunaRepository.findById(id);
    }

    public Optional<Vacuna> findByNombre(String nombre) {
        return vacunaRepository.findByNombre(nombre);
    }

    public Vacuna save(Vacuna vacuna) {
        return vacunaRepository.save(vacuna);
    }

    public void deleteById(Long id) {
        vacunaRepository.deleteById(id);
    }
}

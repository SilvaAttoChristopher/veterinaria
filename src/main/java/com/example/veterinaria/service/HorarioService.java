package com.example.veterinaria.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.veterinaria.modelo.Horario;
import com.example.veterinaria.repository.HorarioRepository;

@Service
public class HorarioService {

    @Autowired
    private HorarioRepository horarioRepository;

    public List<Horario> findAll() {
        return horarioRepository.findAll();
    }

    public Optional<Horario> findById(Long id) {
        return horarioRepository.findById(id);
    }

    public List<Horario> findByUsuarioIdUsuario(Long idUsuario) {
        return horarioRepository.findByVeterinarioIdUsuario(idUsuario);
    }

    public List<Horario> findByUsuarioIdUsuarioAndDiaSemana(Long idUsuario, Short diaSemana) {
        return horarioRepository.findByVeterinarioIdUsuarioAndDiaSemana(idUsuario, diaSemana);
    }

    public Horario save(Horario horario) {
        return horarioRepository.save(horario);
    }

    public void deleteById(Long id) {
        horarioRepository.deleteById(id);
    }
}

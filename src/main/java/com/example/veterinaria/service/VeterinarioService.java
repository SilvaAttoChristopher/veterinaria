package com.example.veterinaria.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.veterinaria.modelo.Veterinario;
import com.example.veterinaria.repository.VeterinarioRepository;

@Service
public class VeterinarioService {

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    public List<Veterinario> findAll() {
        return veterinarioRepository.findAll();
    }

    public Optional<Veterinario> findById(Long id) {
        return veterinarioRepository.findById(id);
    }

    public List<Veterinario> findByEstado(String estado) {
        return veterinarioRepository.findByEstado(estado);
    }

    public Veterinario save(Veterinario veterinario) {
        return veterinarioRepository.save(veterinario);
    }

    /**
     * Actualiza los datos del veterinario. Sólo se copian los campos editables para no
     * sobrescribir con null la contraseña, el rol o el estado.
     */
    @Transactional
    public Veterinario actualizarPerfil(Long id, Veterinario cambios) {
        Veterinario existente = veterinarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Veterinario no encontrado"));

        if (cambios.getNombres() != null) {
            existente.setNombres(cambios.getNombres());
        }
        if (cambios.getApellidos() != null) {
            existente.setApellidos(cambios.getApellidos());
        }
        if (cambios.getEspecialidad() != null) {
            existente.setEspecialidad(cambios.getEspecialidad());
        }
        existente.setCorreo(cambios.getCorreo());
        existente.setTelefono(cambios.getTelefono());
        existente.setDireccion(cambios.getDireccion());

        return veterinarioRepository.save(existente);
    }

    public void deleteById(Long id) {
        veterinarioRepository.deleteById(id);
    }
}

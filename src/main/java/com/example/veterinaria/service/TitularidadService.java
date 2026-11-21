package com.example.veterinaria.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.veterinaria.modelo.Mascota;
import com.example.veterinaria.repository.MascotaRepository;

/**
 * Resuelve la titularidad de una mascota para decidir si quien la consulta es su
 * dueño, el veterinario que la atiende, o un administrador.
 */
@Service
public class TitularidadService {

    @Autowired
    private MascotaRepository mascotaRepository;

    /** id del cliente dueño de la mascota, o empty si la mascota no existe. */
    public Optional<Long> duenoDe(Long idMascota) {
        return mascotaRepository.findById(idMascota)
                .map(Mascota::getCliente)
                .map(cliente -> cliente.getIdUsuario());
    }

    /** true si la mascota pertenece al cliente indicado. */
    public boolean esDueño(Long idMascota, Long idUsuario) {
        if (idMascota == null || idUsuario == null) {
            return false;
        }
        return duenoDe(idMascota).map(idUsuario::equals).orElse(false);
    }
}

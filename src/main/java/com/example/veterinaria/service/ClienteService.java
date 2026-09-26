package com.example.veterinaria.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.veterinaria.modelo.Cliente;
import com.example.veterinaria.repository.ClienteRepository;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> findById(Long id) {
        return clienteRepository.findById(id);
    }

    public List<Cliente> findByDni(String dni) {
        return clienteRepository.findByDni(dni);
    }

    public List<Cliente> buscarPorCriterio(String criterio) {
        return clienteRepository.buscarPorCriterio(criterio);
    }

    public Cliente save(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    /**
     * Actualiza sólo los datos de contacto del cliente.
     *
     * Se carga la entidad existente y se copian únicamente los campos editables, de
     * modo que la contraseña, el DNI y el rol nunca puedan sobrescribirse con null
     * (lo que además rompería la restricción NOT NULL de la contraseña).
     */
    @Transactional
    public Cliente actualizarPerfil(Long id, Cliente cambios) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado"));

        if (cambios.getNombres() != null) {
            existente.setNombres(cambios.getNombres());
        }
        if (cambios.getApellidos() != null) {
            existente.setApellidos(cambios.getApellidos());
        }
        existente.setCorreo(cambios.getCorreo());
        existente.setTelefono(cambios.getTelefono());
        existente.setDireccion(cambios.getDireccion());

        return clienteRepository.save(existente);
    }

    public void deleteById(Long id) {
        clienteRepository.deleteById(id);
    }
}

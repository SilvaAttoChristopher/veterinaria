package com.example.veterinaria.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.veterinaria.modelo.Cartilla;
import com.example.veterinaria.repository.CartillaRepository;

@Service
public class CartillaService {

    @Autowired
    private CartillaRepository cartillaRepository;

    public List<Cartilla> findAll() {
        return cartillaRepository.findAll();
    }

    public Optional<Cartilla> findById(Long id) {
        return cartillaRepository.findById(id);
    }

    public Optional<Cartilla> findByMascotaIdMascota(Long idMascota) {
        return cartillaRepository.findByMascotaIdMascota(idMascota);
    }

    public Cartilla save(Cartilla cartilla) {
        return cartillaRepository.save(cartilla);
    }

    public void deleteById(Long id) {
        cartillaRepository.deleteById(id);
    }
}

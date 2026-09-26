package com.example.veterinaria.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.veterinaria.modelo.Usuario;
import com.example.veterinaria.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    /**
     * Cambia la contraseña del usuario indicado tras verificar la actual.
     *
     * @throws IllegalArgumentException si la contraseña actual no coincide, la nueva
     *                                  no cumple las reglas o es igual a la actual.
     */
    @Transactional
    public Usuario cambiarPassword(String username, String passwordActual,
            String passwordNueva, String passwordConfirmacion) {

        if (passwordActual == null || passwordActual.isBlank()) {
            throw new IllegalArgumentException("Debe introducir su contraseña actual");
        }

        if (passwordNueva == null || passwordNueva.isBlank()) {
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía");
        }

        if (passwordNueva.length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres");
        }

        if (passwordNueva.length() > 72) {
            throw new IllegalArgumentException("La nueva contraseña no puede superar los 72 caracteres");
        }

        if (!passwordNueva.equals(passwordConfirmacion)) {
            throw new IllegalArgumentException("Las contraseñas nuevas no coinciden");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        if (passwordEncoder.matches(passwordNueva, usuario.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser distinta de la actual");
        }

        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        return usuarioRepository.save(usuario);
    }
}

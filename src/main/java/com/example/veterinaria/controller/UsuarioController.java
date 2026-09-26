package com.example.veterinaria.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.veterinaria.modelo.Usuario;
import com.example.veterinaria.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> findAll() {
        return usuarioService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> findById(@PathVariable Long id) {
        return usuarioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Usuario> save(@RequestBody Usuario usuario) {
        Usuario saved = usuarioService.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Actualización genérica de usuario.
     *
     * Con herencia JOINED no se puede hacer merge de una instancia construida desde JSON
     * (tipo concreto Usuario) contra una fila que en realidad es un Cliente o Veterinario:
     * Hibernate lanza WrongClassException. Por eso se carga la entidad existente —que
     * Hibernate devuelve ya con su subclase real— y se mutan sólo los campos editables,
     * preservando contraseña, rol y estado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Usuario usuario) {
        Optional<Usuario> existente = usuarioService.findById(id);
        if (existente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario actual = existente.get();

        if (usuario.getNombres() != null) {
            actual.setNombres(usuario.getNombres());
        }
        if (usuario.getApellidos() != null) {
            actual.setApellidos(usuario.getApellidos());
        }
        actual.setCorreo(usuario.getCorreo());
        actual.setTelefono(usuario.getTelefono());
        actual.setDireccion(usuario.getDireccion());

        return ResponseEntity.ok(usuarioService.save(actual));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!usuarioService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

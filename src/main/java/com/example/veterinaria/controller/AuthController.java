package com.example.veterinaria.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.veterinaria.modelo.Rol;
import com.example.veterinaria.modelo.Usuario;
import com.example.veterinaria.service.RolService;
import com.example.veterinaria.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String nombres = request.get("nombres");
            String apellidos = request.get("apellidos");
            String dni = request.get("dni");
            String telefono = request.get("telefono");
            String correo = request.get("correo");
            String direccion = request.get("direccion");

            if (usuarioService.existsByUsername(username)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "El nombre de usuario ya existe"));
            }

            if (dni != null && !dni.isBlank() && !usuarioService.findByDni(dni.trim()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "El DNI ya está registrado"));
            }

            Rol rolCliente = rolService.findByNombre("CLIENTE")
                    .orElseThrow(() -> new RuntimeException("Rol CLIENTE no encontrado"));

            Usuario cliente = new Usuario();
            cliente.setUsername(username);
            cliente.setPassword(passwordEncoder.encode(password));
            cliente.setNombres(nombres);
            cliente.setApellidos(apellidos);
            cliente.setDni(dni != null && !dni.isBlank() ? dni.trim() : null);
            cliente.setTelefono(telefono);
            cliente.setCorreo(correo != null && !correo.isBlank() ? correo : username);
            cliente.setDireccion(direccion);
            cliente.setRol(rolCliente);

            Usuario saved = usuarioService.save(cliente);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", "Registro exitoso", "id", saved.getIdUsuario().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", "Error en el registro: " + e.getMessage()));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> cambiarPassword(Authentication authentication,
            @RequestBody Map<String, String> request) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "No autenticado"));
        }

        try {
            /* El usuario se toma de la sesión, nunca del cuerpo de la petición. */
            usuarioService.cambiarPassword(
                    authentication.getName(),
                    request.get("passwordActual"),
                    request.get("passwordNueva"),
                    request.get("passwordConfirmacion"));

            return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error al actualizar la contraseña"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "No autenticado"));
        }

        String username = authentication.getName();
        return usuarioService.findByUsername(username)
                .map(usuario -> {
                    /* Map.of admite como máximo 10 pares, así que se usa LinkedHashMap. */
                    Map<String, Object> datos = new LinkedHashMap<>();
                    datos.put("id", usuario.getIdUsuario());
                    datos.put("username", usuario.getUsername());
                    datos.put("nombres", usuario.getNombres());
                    datos.put("apellidos", usuario.getApellidos());
                    datos.put("correo", valorOrVacio(usuario.getCorreo()));
                    datos.put("telefono", valorOrVacio(usuario.getTelefono()));
                    datos.put("direccion", valorOrVacio(usuario.getDireccion()));
                    datos.put("dni", valorOrVacio(usuario.getDni()));
                    datos.put("especialidad", valorOrVacio(usuario.getEspecialidad()));
                    datos.put("estado", usuario.getEstado());
                    datos.put("rol", usuario.getRol().getNombre());
                    return ResponseEntity.ok((Object) datos);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private static String valorOrVacio(String valor) {
        return valor != null ? valor : "";
    }
}

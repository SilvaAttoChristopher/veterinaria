package com.example.veterinaria.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Utilidades de autorización a nivel de controlador. */
public final class Autorizacion {

    private Autorizacion() {
    }

    /** Usuario autenticado en la petición, o null. */
    public static Usuario actual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        return principal instanceof UsuarioPrincipal p
                ? new Usuario(p.getIdUsuario(), auth.getName())
                : null;
    }

    public static boolean esAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public static boolean esVeterinario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VETERINARIO"));
    }

    /** El id corresponde al usuario autenticado. */
    public static boolean esPropio(Long idUsuario) {
        Usuario u = actual();
        return u != null && u.idUsuario().equals(idUsuario);
    }

    /** ADMIN, o bien el propio usuario. */
    public static boolean esPropioOAdmin(Long idUsuario) {
        return esAdmin() || esPropio(idUsuario);
    }

    /** Usuario embebido con únicamente el id y el username. */
    public record Usuario(Long idUsuario, String username) {
    }
}

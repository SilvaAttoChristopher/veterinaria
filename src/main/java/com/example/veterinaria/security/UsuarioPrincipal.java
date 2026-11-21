package com.example.veterinaria.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * UserDetails que conserva el id del usuario.
 *
 * Spring Security compara por nombre de usuario, pero para autorizar el acceso
 * "a su propio recurso" (por ejemplo PUT /api/usuarios/{id}) hace falta conocer
 * el id. Llevarlo en el principal evita una consulta a base de datos en cada
 * petición.
 */
public class UsuarioPrincipal extends User {

    private final Long idUsuario;

    public UsuarioPrincipal(Long idUsuario, String username, String password,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.idUsuario = idUsuario;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }
}

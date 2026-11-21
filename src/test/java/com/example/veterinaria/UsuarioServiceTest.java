
package com.example.veterinaria;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;
import java.util.Optional;

import com.example.veterinaria.modelo.Usuario;
import com.example.veterinaria.repository.UsuarioRepository;
import com.example.veterinaria.repository.RolRepository;
import com.example.veterinaria.service.UsuarioService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void buscarUsuarioDebeEncontrarUnUsuarioExistente() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setUsername("cliente01");

        when(usuarioRepository.findByUsername("cliente01"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado =
                usuarioService.findByUsername("cliente01");

        assertTrue(resultado.isPresent());
        assertEquals("cliente01", resultado.get().getUsername());
    }

    @Test
    void cambiarEstadoDebeActualizarElEstadoDelUsuario() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setEstado("ACTIVO");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado =
                usuarioService.cambiarEstado(1L, "INACTIVO");

        assertEquals("INACTIVO", resultado.getEstado());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarEstadoDebeRechazarUnEstadoNoValido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.cambiarEstado(1L, "BLOQUEADO")
        );

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_siUsuarioNoExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(
                NoSuchElementException.class,
                () -> usuarioService.cambiarEstado(1L, "INACTIVO")
        );
        verify(usuarioRepository).findById(1L);
    }
}

package com.example.veterinaria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.example.veterinaria.modelo.Usuario;
import com.example.veterinaria.repository.UsuarioRepository;
import com.example.veterinaria.service.FotoPerfilService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FotosdePerfilTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @TempDir
    private Path carpetaTemporal;

    private FotoPerfilService fotoPerfilService;

    @BeforeEach
    void setUp() throws IOException {
        fotoPerfilService = new FotoPerfilService(usuarioRepository, carpetaTemporal.toString());
    }

    @Test
    void guardarFotoPerfilDebeActualizarLaRutaYGuardarElArchivo() throws IOException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setFotoPerfil("default.jpg");

        byte[] contenido = "contenido-de-prueba".getBytes();
        MultipartFile archivo = new MockMultipartFile(
                "foto", "perfil_nuevo.jpg", "image/jpeg", contenido);

        when(usuarioRepository.findByUsername("sergio"))
                .thenReturn(Optional.of(usuario));

        fotoPerfilService.guardar("sergio", archivo);

        assertNotEquals("default.jpg", usuario.getFotoPerfil());
        assertTrue(Files.exists(carpetaTemporal.resolve(usuario.getFotoPerfil())));
        assertEquals(new String(contenido),
                Files.readString(carpetaTemporal.resolve(usuario.getFotoPerfil())));
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void guardarFotoPerfilDebeLanzarExcepcionSiUsuarioNoExiste() {
        MultipartFile archivo = new MockMultipartFile(
                "foto", "perfil_nuevo.jpg", "image/jpeg", "bytes".getBytes());

        when(usuarioRepository.findByUsername("desconocido"))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> fotoPerfilService.guardar("desconocido", archivo));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}

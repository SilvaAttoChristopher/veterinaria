package com.example.veterinaria.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.veterinaria.modelo.Usuario;
import com.example.veterinaria.repository.UsuarioRepository;

@Service
public class FotoPerfilService {

    private final UsuarioRepository usuarioRepository;
    private final Path carpeta;

    public FotoPerfilService(UsuarioRepository usuarioRepository,
                             @Value("${app.uploads.fotos-perfil}") String ruta) throws IOException {
        this.usuarioRepository = usuarioRepository;
        this.carpeta = Path.of(ruta);
        Files.createDirectories(carpeta);
    }

    public void guardar(String username, MultipartFile archivo) throws IOException {
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("Selecciona una imagen");
        }
        if (archivo.getSize() > 800 * 1024) {
            throw new IllegalArgumentException("La imagen supera el máximo de 800 KB");
        }

        String tipo = archivo.getContentType();
        String extension;
        if ("image/jpeg".equals(tipo)) {
            extension = "jpg";
        } else if ("image/png".equals(tipo)) {
            extension = "png";
        } else if ("image/gif".equals(tipo)) {
            extension = "gif";
        } else {
            throw new IllegalArgumentException("Formato no permitido. Usa JPG, PNG o GIF");
        }

        Usuario usuario = usuarioRepository.findByUsername(username).orElseThrow();

        String anterior = usuario.getFotoPerfil();
        if (anterior != null) {
            Files.deleteIfExists(carpeta.resolve(anterior));
        }

        String nombre = "usuario-" + usuario.getIdUsuario() + "-" + System.currentTimeMillis() + "." + extension;
        Files.write(carpeta.resolve(nombre), archivo.getBytes());

        usuario.setFotoPerfil(nombre);
        usuarioRepository.save(usuario);
    }

    public void eliminar(String username) throws IOException {
        Usuario usuario = usuarioRepository.findByUsername(username).orElseThrow();

        String anterior = usuario.getFotoPerfil();
        if (anterior != null) {
            Files.deleteIfExists(carpeta.resolve(anterior));
        }

        usuario.setFotoPerfil(null);
        usuarioRepository.save(usuario);
    }

    public Path obtenerRuta(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username).orElseThrow();

        if (usuario.getFotoPerfil() == null) {
            return null;
        }
        return carpeta.resolve(usuario.getFotoPerfil());
    }
}
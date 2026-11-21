package com.example.veterinaria.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.veterinaria.service.FotoPerfilService;

@RestController
@RequestMapping("/api/perfil/foto")
public class PerfilController {

    @Autowired
    private FotoPerfilService fotoPerfilService;

    @GetMapping
    public ResponseEntity<Resource> obtener(Authentication authentication) {
        Path ruta = fotoPerfilService.obtenerRuta(authentication.getName());

        if (ruta == null || !Files.exists(ruta)) {
            return ResponseEntity.notFound().build();
        }

        MediaType tipo = MediaTypeFactory.getMediaType(ruta.getFileName().toString())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok().contentType(tipo).body(new FileSystemResource(ruta));
    }

    @PostMapping
    public ResponseEntity<?> subir(Authentication authentication,
                                   @RequestParam("foto") MultipartFile foto) {
        try {
            fotoPerfilService.guardar(authentication.getName(), foto);
            return ResponseEntity.ok(Map.of("mensaje", "Foto actualizada"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "No se pudo guardar la imagen"));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> restablecer(Authentication authentication) {
        try {
            fotoPerfilService.eliminar(authentication.getName());
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "No se pudo eliminar la imagen"));
        }
    }
}
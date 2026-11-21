
package com.example.veterinaria;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.example.veterinaria.modelo.Mascota;
import com.example.veterinaria.repository.MascotaRepository;
import com.example.veterinaria.service.MascotaService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MascotaServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @InjectMocks
    private MascotaService mascotaService;

    @Test
    void findAllDebeDevolverLaListaDeMascotas() {
        List<Mascota> mascotas = List.of(
                new Mascota(null, "Luna", "Perro"),
                new Mascota(null, "Michi", "Gato")
        );

        when(mascotaRepository.findAll()).thenReturn(mascotas);

        List<Mascota> resultado = mascotaService.findAll();

        assertEquals(2, resultado.size());
        assertEquals("Luna", resultado.get(0).getNombre());
        assertEquals("Michi", resultado.get(1).getNombre());
    }

    @Test
    void findByIdDebeEncontrarUnaMascota() {
        Mascota mascota = new Mascota(null, "Luna", "Perro");

        when(mascotaRepository.findById(1L))
                .thenReturn(Optional.of(mascota));

        Optional<Mascota> resultado = mascotaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Luna", resultado.get().getNombre());
    }

    @Test
    void saveDebeGuardarUnaMascota() {
        Mascota mascota = new Mascota(null, "Rocky", "Perro");

        when(mascotaRepository.save(mascota)).thenReturn(mascota);

        Mascota resultado = mascotaService.save(mascota);

        assertNotNull(resultado);
        assertEquals("Rocky", resultado.getNombre());
        assertEquals("Perro", resultado.getEspecie());

        verify(mascotaRepository).save(mascota);
    }
}

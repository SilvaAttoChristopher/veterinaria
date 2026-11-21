
package com.example.veterinaria;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.veterinaria.modelo.Cita;
import com.example.veterinaria.repository.CitaRepository;
import com.example.veterinaria.repository.MascotaRepository;
import com.example.veterinaria.service.CitaService;
import com.example.veterinaria.service.HorarioService;
import com.example.veterinaria.service.NotificacionService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private HorarioService horarioService;

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private CitaService citaService;

    @Test
    void findAllDebeDevolverLasCitasRegistradas() {
        Cita cita1 = new Cita();
        Cita cita2 = new Cita();

        when(citaRepository.findAll())
                .thenReturn(List.of(cita1, cita2));

        List<Cita> resultado = citaService.findAll();

        assertEquals(2, resultado.size());
        verify(citaRepository).findAll();
    }

    @Test
    void findByIdDebeEncontrarUnaCitaExistente() {
        Cita cita = new Cita();
        cita.setIdCita(1L);
        cita.setEstado("PROGRAMADA");

        when(citaRepository.findById(1L))
                .thenReturn(Optional.of(cita));

        Optional<Cita> resultado = citaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("PROGRAMADA", resultado.get().getEstado());
    }

    @Test
    void findByEstadoDebeDevolverLasCitasDelEstadoIndicado() {
        Cita cita = new Cita();
        cita.setEstado("PROGRAMADA");

        when(citaRepository.findByEstado("PROGRAMADA"))
                .thenReturn(List.of(cita));

        List<Cita> resultado =
                citaService.findByEstado("PROGRAMADA");

        assertEquals(1, resultado.size());
        assertEquals("PROGRAMADA", resultado.get(0).getEstado());
        verify(citaRepository).findByEstado("PROGRAMADA");
    }

    @Test
    void buscarParaReporteDebePedirLasCitasAlRepositorio() {
        Cita cita = new Cita();
        cita.setEstado("ATENDIDA");

        when(citaRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(cita));

        List<Cita> resultado = citaService.buscarParaReporte(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                "programada",
                5L,
                null,
                null);

        assertEquals(1, resultado.size());
        assertEquals("ATENDIDA", resultado.get(0).getEstado());
        verify(citaRepository).findAll(any(Specification.class));
    }

    @Test
    void buscarParaReporteSinFiltrosTambienDevuelveResultados() {
        when(citaRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(new Cita(), new Cita()));

        List<Cita> resultado = citaService.buscarParaReporte(
                null, null, null, null, null, null);

        assertEquals(2, resultado.size());
    }
}

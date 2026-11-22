
package com.example.veterinaria;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.veterinaria.modelo.Cita;
import com.example.veterinaria.modelo.Mascota;
import com.example.veterinaria.modelo.Pago;
import com.example.veterinaria.modelo.Servicio;
import com.example.veterinaria.modelo.Usuario;
import com.example.veterinaria.repository.CitaRepository;
import com.example.veterinaria.repository.MascotaRepository;
import com.example.veterinaria.repository.PagoRepository;
import com.example.veterinaria.repository.ServicioRepository;
import com.example.veterinaria.service.CitaService;
import com.example.veterinaria.service.NotificacionService;
import com.example.veterinaria.service.PagoService;
import com.example.veterinaria.service.StripeService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private CitaService citaService;

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private StripeService stripeService;

    @InjectMocks
    private PagoService pagoService;

    private static final Long CLIENTE = 10L;
    private static final Long ID_MASCOTA = 5L;
    private static final Long ID_SERVICIO = 3L;

    // ------------------------------------------------------------ crear checkout

    @Test
    void crearCheckoutReservaLaCitaYDevuelveLaUrlDeStripe() throws Exception {
        Mascota mascota = mascotaConCliente(ID_MASCOTA, CLIENTE);
        Servicio servicio = new Servicio("Consulta", new BigDecimal("80.00"));
        servicio.setIdServicio(ID_SERVICIO);

        Cita solicitud = citaNueva(mascota, servicio);
        Cita guardada = citaNueva(mascota, servicio);
        guardada.setIdCita(7L);

        when(stripeService.configurado()).thenReturn(true);
        when(mascotaRepository.findById(ID_MASCOTA)).thenReturn(Optional.of(mascota));
        when(servicioRepository.findById(ID_SERVICIO)).thenReturn(Optional.of(servicio));
        when(citaService.save(any(Cita.class))).thenReturn(guardada);
        when(pagoRepository.findFirstByCitaIdCitaOrderByFechaCreacionDesc(7L))
                .thenReturn(Optional.empty());
        when(stripeService.crearSesionCheckout(any(), any(), any(), any(), any(), any()))
                .thenReturn(new StripeService.SesionCheckout("cs_123",
                        "https://checkout.stripe.com/pay/cs_123"));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        PagoService.SesionPago resultado = pagoService.crearCheckout(solicitud, CLIENTE);

        assertEquals("https://checkout.stripe.com/pay/cs_123", resultado.url());
        assertEquals("cs_123", resultado.sessionId());
        assertEquals(new BigDecimal("80.00"), resultado.monto());
        assertEquals("pen", resultado.moneda());

        /* La cita se crea reservando el horario, nunca como programada. */
        ArgumentCaptor<Cita> captor = ArgumentCaptor.forClass(Cita.class);
        verify(citaService).save(captor.capture());
        assertEquals(PagoService.CITA_PENDIENTE_PAGO, captor.getValue().getEstado());

        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pagoCaptor.capture());
        assertEquals(PagoService.PAGO_PENDIENTE, pagoCaptor.getValue().getEstado());
        assertEquals("cs_123", pagoCaptor.getValue().getStripeSessionId());
    }

    @Test
    void crearCheckoutRechazaUnaMascotaQueNoEsDelCliente() throws Exception {
        Mascota mascota = mascotaConCliente(ID_MASCOTA, 99L);
        Servicio servicio = new Servicio("Consulta", new BigDecimal("80.00"));
        servicio.setIdServicio(ID_SERVICIO);

        when(stripeService.configurado()).thenReturn(true);
        when(mascotaRepository.findById(ID_MASCOTA)).thenReturn(Optional.of(mascota));

        Cita solicitud = citaNueva(mascota, servicio);

        assertThrows(SecurityException.class,
                () -> pagoService.crearCheckout(solicitud, CLIENTE));
        verify(pagoRepository, never()).save(any());
        verify(stripeService, never()).crearSesionCheckout(any(), any(), any(), any(), any(), any());
    }

    @Test
    void crearCheckoutFallaSinClaveDeStripe() throws Exception {
        when(stripeService.configurado()).thenReturn(false);

        Cita solicitud = citaNueva(mascotaConCliente(ID_MASCOTA, CLIENTE), new Servicio());

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> pagoService.crearCheckout(solicitud, CLIENTE));
        assertTrue(e.getMessage().contains("no está configurada"));
        verify(citaService, never()).save(any());
    }

    @Test
    void crearCheckoutReintentaElPagoDeUnaCitaPendiente() throws Exception {
        Cita pendiente = citaNueva(mascotaConCliente(ID_MASCOTA, CLIENTE),
                new Servicio("Consulta", new BigDecimal("50.00")));
        pendiente.setIdCita(7L);
        pendiente.setEstado(PagoService.CITA_PENDIENTE_PAGO);
        pendiente.getServicio().setIdServicio(ID_SERVICIO);

        Servicio servicio = new Servicio("Consulta", new BigDecimal("50.00"));
        servicio.setIdServicio(ID_SERVICIO);

        Pago anterior = new Pago();
        anterior.setEstado(PagoService.PAGO_PENDIENTE);

        when(stripeService.configurado()).thenReturn(true);
        when(citaRepository.findById(7L)).thenReturn(Optional.of(pendiente));
        when(servicioRepository.findById(ID_SERVICIO)).thenReturn(Optional.of(servicio));
        when(pagoRepository.findFirstByCitaIdCitaOrderByFechaCreacionDesc(7L))
                .thenReturn(Optional.of(anterior));
        when(stripeService.crearSesionCheckout(any(), any(), any(), any(), any(), any()))
                .thenReturn(new StripeService.SesionCheckout("cs_456", "https://stripe/csq"));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        Cita soloId = new Cita();
        soloId.setIdCita(7L);

        PagoService.SesionPago resultado = pagoService.crearCheckout(soloId, CLIENTE);

        assertEquals(7L, resultado.idCita());
        assertEquals("cs_456", resultado.sessionId());
        /* No se vuelve a crear la cita: sólo se abre otra sesión de pago. */
        verify(citaService, never()).save(any());
        assertEquals("cs_456", anterior.getStripeSessionId());
    }

    @Test
    void crearCheckoutLiberaUnaReservaSinPagoVencida() throws Exception {
        Mascota mascota = mascotaConCliente(ID_MASCOTA, CLIENTE);
        Servicio servicio = new Servicio("Consulta", new BigDecimal("80.00"));
        servicio.setIdServicio(ID_SERVICIO);

        Cita solicitud = citaNueva(mascota, servicio);
        Cita guardada = citaNueva(mascota, servicio);
        guardada.setIdCita(7L);

        Cita vencida = new Cita();
        vencida.setIdCita(9L);
        vencida.setEstado(PagoService.CITA_PENDIENTE_PAGO);

        Pago pagoVencido = new Pago();
        pagoVencido.setEstado(PagoService.PAGO_PENDIENTE);
        pagoVencido.setFechaCreacion(LocalDateTime.now().minusHours(2));

        when(stripeService.configurado()).thenReturn(true);
        when(mascotaRepository.findById(ID_MASCOTA)).thenReturn(Optional.of(mascota));
        when(servicioRepository.findById(ID_SERVICIO)).thenReturn(Optional.of(servicio));
        when(citaRepository.findByVeterinarioIdUsuarioAndFechaAndHoraAndEstado(
                anyLong(), any(), any(), eq(PagoService.CITA_PENDIENTE_PAGO)))
                .thenReturn(List.of(vencida));
        when(pagoRepository.findFirstByCitaIdCitaOrderByFechaCreacionDesc(9L))
                .thenReturn(Optional.of(pagoVencido));
        when(citaService.save(any(Cita.class))).thenReturn(guardada);
        when(pagoRepository.findFirstByCitaIdCitaOrderByFechaCreacionDesc(7L))
                .thenReturn(Optional.empty());
        when(stripeService.crearSesionCheckout(any(), any(), any(), any(), any(), any()))
                .thenReturn(new StripeService.SesionCheckout("cs_789", "https://stripe/csq"));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        pagoService.crearCheckout(solicitud, CLIENTE);

        assertEquals(PagoService.CITA_CANCELADA, vencida.getEstado());
        assertEquals(PagoService.PAGO_CANCELADO, pagoVencido.getEstado());
    }

    @Test
    void crearCheckoutUsaLaUrlPublicaDeLaPeticionParaLosRetornosDeStripe() throws Exception {
        Mascota mascota = mascotaConCliente(ID_MASCOTA, CLIENTE);
        Servicio servicio = new Servicio("Consulta", new BigDecimal("80.00"));
        servicio.setActivo(true);
        servicio.setIdServicio(ID_SERVICIO);

        Cita solicitud = citaNueva(mascota, servicio);
        Cita guardada = citaNueva(mascota, servicio);
        guardada.setIdCita(7L);

        when(stripeService.configurado()).thenReturn(true);
        when(mascotaRepository.findById(ID_MASCOTA)).thenReturn(Optional.of(mascota));
        when(servicioRepository.findById(ID_SERVICIO)).thenReturn(Optional.of(servicio));
        when(citaService.save(any(Cita.class))).thenReturn(guardada);
        when(pagoRepository.findFirstByCitaIdCitaOrderByFechaCreacionDesc(7L))
                .thenReturn(Optional.empty());
        when(stripeService.crearSesionCheckout(any(), any(), any(), any(), any(), any()))
                .thenReturn(new StripeService.SesionCheckout("cs_999", "https://stripe/csq"));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        /* La URL pública de la petición debe llegar a Stripe sin cambios. */
        pagoService.crearCheckout(solicitud, CLIENTE, "https://vet.example.com:8443");

        verify(stripeService).crearSesionCheckout(any(), any(), any(), any(), any(),
                eq("https://vet.example.com:8443"));
    }

    // ------------------------------------------------------------ confirmar pago

    @Test
    void procesarPagoExitosoConfirmaElPagoYProgramaLaCita() throws Exception {
        Cita cita = citaPendiente();
        Pago pago = pagoDe(cita, PagoService.PAGO_PENDIENTE);

        when(pagoRepository.findByStripeSessionId("cs_1")).thenReturn(Optional.of(pago));
        when(stripeService.obtenerSesion("cs_1"))
                .thenReturn(new StripeService.SesionInfo("cs_1", "paid", "pi_1", Map.of()));

        Optional<Pago> resultado = pagoService.procesarPagoExitoso("cs_1");

        assertTrue(resultado.isPresent());
        assertEquals(PagoService.PAGO_PAGADO, pago.getEstado());
        assertNotNull(pago.getFechaPago());
        assertEquals("pi_1", pago.getStripePaymentIntentId());
        assertEquals(PagoService.CITA_PROGRAMADA, cita.getEstado());
        verify(pagoRepository).save(pago);
        verify(citaRepository).save(cita);
        verify(notificacionService).save(any());
    }

    @Test
    void procesarPagoExitosoNoSeRepiteSiYaEstabaConfirmado() throws Exception {
        Cita cita = citaPendiente();
        cita.setEstado(PagoService.CITA_PROGRAMADA);
        Pago pago = pagoDe(cita, PagoService.PAGO_PAGADO);

        when(pagoRepository.findByStripeSessionId("cs_1")).thenReturn(Optional.of(pago));

        Optional<Pago> resultado = pagoService.procesarPagoExitoso("cs_1");

        assertTrue(resultado.isPresent());
        verify(stripeService, never()).obtenerSesion(any());
        verify(notificacionService, never()).save(any());
    }

    @Test
    void procesarPagoExitosoNoConfirmaSinPagoAprobado() throws Exception {
        Cita cita = citaPendiente();
        Pago pago = pagoDe(cita, PagoService.PAGO_PENDIENTE);

        when(pagoRepository.findByStripeSessionId("cs_1")).thenReturn(Optional.of(pago));
        when(stripeService.obtenerSesion("cs_1"))
                .thenReturn(new StripeService.SesionInfo("cs_1", "unpaid", null, Map.of()));

        Optional<Pago> resultado = pagoService.procesarPagoExitoso("cs_1");

        assertTrue(resultado.isPresent());
        assertEquals(PagoService.PAGO_PENDIENTE, pago.getEstado());
        assertEquals(PagoService.CITA_PENDIENTE_PAGO, cita.getEstado());
        verify(notificacionService, never()).save(any());
    }

    @Test
    void procesarPagoExitosoIgnoraSesionesDesconocidas() throws Exception {
        when(pagoRepository.findByStripeSessionId("cs_x")).thenReturn(Optional.empty());

        assertTrue(pagoService.procesarPagoExitoso("cs_x").isEmpty());
        verify(stripeService, never()).obtenerSesion(any());
    }

    // ------------------------------------------------------------ cancelar pago

    @Test
    void cancelarPorSesionLiberaLaReserva() {
        Cita cita = citaPendiente();
        Pago pago = pagoDe(cita, PagoService.PAGO_PENDIENTE);
        pago.setStripeSessionId("cs_9");

        when(pagoRepository.findByStripeSessionId("cs_9")).thenReturn(Optional.of(pago));

        pagoService.cancelarPorSesion("cs_9");

        assertEquals(PagoService.PAGO_CANCELADO, pago.getEstado());
        assertEquals(PagoService.CITA_CANCELADA, cita.getEstado());
        verify(citaRepository).save(cita);
    }

    @Test
    void cancelarPorSesionNoTocaUnPagoYaRealizado() {
        Cita cita = citaPendiente();
        cita.setEstado(PagoService.CITA_PROGRAMADA);
        Pago pago = pagoDe(cita, PagoService.PAGO_PAGADO);
        pago.setStripeSessionId("cs_9");

        when(pagoRepository.findByStripeSessionId("cs_9")).thenReturn(Optional.of(pago));

        pagoService.cancelarPorSesion("cs_9");

        assertEquals(PagoService.PAGO_PAGADO, pago.getEstado());
        assertEquals(PagoService.CITA_PROGRAMADA, cita.getEstado());
        verify(citaRepository, never()).save(any());
    }

    @Test
    void cancelarPorCitaCierraLasSesionesPendientes() {
        Cita cita = citaPendiente();
        cita.setIdCita(7L);
        Pago pago = pagoDe(cita, PagoService.PAGO_PENDIENTE);
        pago.setStripeSessionId("cs_5");

        when(pagoRepository.findByCitaIdCita(7L)).thenReturn(List.of(pago));

        pagoService.cancelarPorCita(7L);

        assertEquals(PagoService.PAGO_CANCELADO, pago.getEstado());
        verify(stripeService).expirarSesion("cs_5");
    }

    // ------------------------------------------------------------ retorno / ayuda

    @Test
    void procesarRetornoReportaElPagoConfirmado() throws Exception {
        Cita cita = citaPendiente();
        Pago pago = pagoDe(cita, PagoService.PAGO_PENDIENTE);

        when(pagoRepository.findByStripeSessionId("cs_1")).thenReturn(Optional.of(pago));
        when(stripeService.obtenerSesion("cs_1"))
                .thenReturn(new StripeService.SesionInfo("cs_1", "paid", "pi_1", Map.of()));

        PagoService.ResultadoRetorno resultado = pagoService.procesarRetorno("cs_1");

        assertEquals(PagoService.PAGO_PAGADO, resultado.estado());
        assertEquals(7L, resultado.idCita());
        assertTrue(resultado.mensaje().contains("Pago confirmado"));
    }

    @Test
    void procesarRetornoSinSesionDevuelveDesconocido() {
        PagoService.ResultadoRetorno resultado = pagoService.procesarRetorno("  ");

        assertEquals("DESCONOCIDO", resultado.estado());
        assertNull(resultado.idCita());
    }

    @Test
    void montoEnCentesimosRedondeaDeFormaSegura() {
        assertEquals(8000L, StripeService.montoCents(new BigDecimal("80.00")));
        assertEquals(9999L, StripeService.montoCents(new BigDecimal("99.99")));
        assertEquals(4550L, StripeService.montoCents(new BigDecimal("45.5")));
        assertEquals(100L, StripeService.montoCents(new BigDecimal("1")));
    }

    // ------------------------------------------------------------ helpers

    private Mascota mascotaConCliente(Long idMascota, Long idCliente) {
        Usuario cliente = new Usuario();
        cliente.setIdUsuario(idCliente);
        cliente.setCorreo("cliente@vet.pe");

        Mascota mascota = new Mascota();
        mascota.setIdMascota(idMascota);
        mascota.setNombre("Firulais");
        mascota.setCliente(cliente);
        return mascota;
    }

    private Cita citaNueva(Mascota mascota, Servicio servicio) {
        Cita cita = new Cita();
        cita.setMascota(mascota);
        cita.setServicio(servicio);

        Usuario vet = new Usuario();
        vet.setIdUsuario(2L);
        cita.setVeterinario(vet);

        cita.setFecha(LocalDate.now().plusDays(1));
        cita.setHora(LocalTime.of(10, 0));
        return cita;
    }

    private Cita citaPendiente() {
        Cita cita = new Cita();
        cita.setIdCita(7L);
        cita.setEstado(PagoService.CITA_PENDIENTE_PAGO);
        cita.setFecha(LocalDate.now().plusDays(1));
        cita.setHora(LocalTime.of(10, 0));
        cita.setMascota(mascotaConCliente(ID_MASCOTA, CLIENTE));

        Usuario vet = new Usuario();
        vet.setIdUsuario(2L);
        cita.setVeterinario(vet);

        Servicio servicio = new Servicio("Consulta", new BigDecimal("80.00"));
        servicio.setIdServicio(ID_SERVICIO);
        cita.setServicio(servicio);
        return cita;
    }

    private Pago pagoDe(Cita cita, String estado) {
        Pago pago = new Pago();
        pago.setCita(cita);
        pago.setEstado(estado);
        pago.setMonto(new BigDecimal("80.00"));
        pago.setMoneda("pen");
        pago.setStripeSessionId("cs_1");
        pago.setFechaCreacion(LocalDateTime.now());
        return pago;
    }
}

package com.example.veterinaria.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.veterinaria.modelo.Cita;
import com.example.veterinaria.modelo.Mascota;
import com.example.veterinaria.modelo.Notificacion;
import com.example.veterinaria.modelo.Pago;
import com.example.veterinaria.modelo.Servicio;
import com.example.veterinaria.repository.CitaRepository;
import com.example.veterinaria.repository.MascotaRepository;
import com.example.veterinaria.repository.PagoRepository;
import com.example.veterinaria.repository.ServicioRepository;
import com.example.veterinaria.security.Autorizacion;
import com.stripe.exception.StripeException;

/**
 * Reglas de negocio de la pasarela de pagos.
 *
 * Flujo: al agendar una cita se crea primero con estado PENDIENTE_PAGO (reserva
 * el horario), se cobra con Stripe Checkout y sólo al confirmar el pago la cita
 * pasa a PROGRAMADA. Si el pago se abandona o se cancela, la reserva se libera.
 */
@Service
public class PagoService {

    public static final String CITA_PENDIENTE_PAGO = "PENDIENTE_PAGO";
    public static final String CITA_PROGRAMADA = "PROGRAMADA";
    public static final String CITA_CANCELADA = "CANCELADA";

    public static final String PAGO_PENDIENTE = "PENDIENTE";
    public static final String PAGO_PAGADO = "PAGADO";
    public static final String PAGO_CANCELADO = "CANCELADO";

    /** Una reserva sin pago se libera después de este tiempo. */
    private static final Duration VIGENCIA_RESERVA = Duration.ofMinutes(30);

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private CitaService citaService;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private StripeService stripeService;

    /**
     * Crea (o reintenta) el pago de una cita y devuelve la URL de Stripe Checkout.
     *
     * @param solicitud cita nueva, o bien una cita existente con idCita para
     *                  reintentar el pago de una reserva pendiente.
     * @param idCliente usuario autenticado que debe ser dueño de la mascota.
     */
    /* También transaccional: si sólo lo fuera la de 3 argumentos, la llamada
       interna no pasaría por el proxy de Spring y no habría transacción. */
    @Transactional(rollbackFor = Exception.class)
    public SesionPago crearCheckout(Cita solicitud, Long idCliente) throws StripeException {
        return crearCheckout(solicitud, idCliente, null);
    }

    /**
     * Igual que {@link #crearCheckout(Cita, Long)} indicando la URL pública desde
     * la que llega la petición, para que los retornos de Stripe funcionen aunque
     * cambie el dominio del deploy.
     *
     * @param urlBase URL pública deducida de la petición; puede ser null.
     */
    /* rollbackFor: StripeException es checked y por defecto NO revierte la transacción,
       lo que dejaría una cita PENDIENTE_PAGO huérfana bloqueando el horario.
       Va también en la sobrecarga de 2 argumentos porque al invocarse a sí misma
       no pasa por el proxy de Spring y no habría transacción. */
    @Transactional(rollbackFor = Exception.class)
    public SesionPago crearCheckout(Cita solicitud, Long idCliente, String urlBase)
            throws StripeException {
        if (!stripeService.configurado()) {
            throw new IllegalStateException("La pasarela de pago no está configurada");
        }
        if (solicitud == null) {
            throw new IllegalArgumentException("La cita debe indicar la mascota");
        }

        Cita cita;
        Mascota mascota;
        Servicio servicio;

        if (solicitud.getIdCita() != null) {
            /* Reintento de pago de una reserva ya creada: todo se deduce de la cita. */
            cita = citaRepository.findById(solicitud.getIdCita())
                    .orElseThrow(() -> new IllegalArgumentException("La cita no existe"));
            if (!esDelCliente(cita.getMascota(), idCliente)) {
                throw new SecurityException("La cita no le pertenece");
            }
            if (!CITA_PENDIENTE_PAGO.equals(cita.getEstado())) {
                throw new IllegalStateException("La cita ya no tiene pagos pendientes");
            }
            mascota = cita.getMascota();
            servicio = servicioRepository.findById(cita.getServicio().getIdServicio())
                    .orElseThrow(() -> new IllegalArgumentException("El servicio no existe"));
        } else {
            if (solicitud.getMascota() == null || solicitud.getMascota().getIdMascota() == null) {
                throw new IllegalArgumentException("La cita debe indicar la mascota");
            }
            if (solicitud.getServicio() == null || solicitud.getServicio().getIdServicio() == null) {
                throw new IllegalArgumentException("La cita debe indicar el servicio");
            }
            if (solicitud.getVeterinario() == null
                    || solicitud.getVeterinario().getIdUsuario() == null) {
                throw new IllegalArgumentException("La cita debe indicar el veterinario");
            }
            if (solicitud.getFecha() == null || solicitud.getHora() == null) {
                throw new IllegalArgumentException("La cita debe indicar fecha y hora");
            }

            mascota = mascotaRepository.findById(solicitud.getMascota().getIdMascota())
                    .orElseThrow(() -> new IllegalArgumentException("La mascota no existe"));
            if (!esDelCliente(mascota, idCliente)) {
                throw new SecurityException("La mascota no le pertenece");
            }

            servicio = servicioRepository.findById(solicitud.getServicio().getIdServicio())
                    .orElseThrow(() -> new IllegalArgumentException("El servicio no existe"));

            liberarReservasVencidas(solicitud.getVeterinario().getIdUsuario(),
                    solicitud.getFecha(), solicitud.getHora());
            solicitud.setEstado(CITA_PENDIENTE_PAGO);
            /* Valida el horario y avisa al dueño en la misma transacción. */
            cita = citaService.save(solicitud);
        }

        if (!Boolean.TRUE.equals(servicio.getActivo())) {
            throw new IllegalArgumentException("El servicio ya no está disponible");
        }
        if (servicio.getPrecio() == null || servicio.getPrecio().signum() <= 0) {
            throw new IllegalArgumentException("El servicio no tiene un precio definido");
        }

        Pago pago = pagoRepository
                .findFirstByCitaIdCitaOrderByFechaCreacionDesc(cita.getIdCita())
                .orElseGet(Pago::new);
        if (PAGO_PAGADO.equals(pago.getEstado())) {
            throw new IllegalStateException("La cita ya está pagada");
        }

        BigDecimal monto = servicio.getPrecio();

        pago.setCita(cita);
        pago.setMonto(monto);
        pago.setMoneda(StripeService.MONEDA);
        pago.setEstado(PAGO_PENDIENTE);
        pago.setFechaCreacion(LocalDateTime.now());
        pago.setFechaPago(null);
        pago.setStripePaymentIntentId(null);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("idCita", String.valueOf(cita.getIdCita()));
        metadata.put("idCliente", String.valueOf(idCliente));
        if (pago.getIdPago() != null) {
            metadata.put("idPago", String.valueOf(pago.getIdPago()));
        }

        StripeService.SesionCheckout sesion = stripeService.crearSesionCheckout(
                monto,
                "Cita: " + servicio.getNombre(),
                String.format("%s - %s %s",
                        mascota.getNombre(),
                        cita.getFecha().format(FORMATO_FECHA),
                        cita.getHora()),
                mascota.getCliente() != null ? mascota.getCliente().getCorreo() : null,
                metadata,
                urlBase);

        pago.setStripeSessionId(sesion.id());
        Pago guardado = pagoRepository.save(pago);

        return new SesionPago(guardado.getIdPago(), cita.getIdCita(),
                sesion.id(), sesion.url(), monto, StripeService.MONEDA);
    }

    /**
     * Confirma el pago si Stripe dice que la sesión está paga. Idempotente: si ya
     * estaba confirmado no vuelve a notificar.
     *
     * @return el pago, vacío si la sesión no corresponde a ningún pago.
     */
    @Transactional
    public Optional<Pago> procesarPagoExitoso(String sessionId) throws StripeException {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }

        Pago pago = pagoRepository.findByStripeSessionId(sessionId).orElse(null);
        if (pago == null) {
            return Optional.empty();
        }
        if (PAGO_PAGADO.equals(pago.getEstado())) {
            return Optional.of(pago);
        }

        StripeService.SesionInfo sesion = stripeService.obtenerSesion(sessionId);
        if (!"paid".equals(sesion.estadoPago())) {
            /* Todavía no se cobra (medios de pago asíncronos): se espera otro evento. */
            return Optional.of(pago);
        }

        return Optional.of(confirmar(pago, sesion));
    }

    /** Libera una reserva cuyo pago se abandonó (y su sesión venció en Stripe). */
    @Transactional
    public void cancelarPorSesion(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        pagoRepository.findByStripeSessionId(sessionId).ifPresent(pago -> {
            if (PAGO_PAGADO.equals(pago.getEstado())) {
                return;
            }
            pago.setEstado(PAGO_CANCELADO);
            pagoRepository.save(pago);
            liberarCita(pago);
        });
    }

    /**
     * El cliente descartó una cita pendiente de pago: se cancela la reserva y se
     * cierra la sesión de Stripe para que nadie pueda pagarla después.
     */
    @Transactional
    public void cancelarPorCita(Long idCita) {
        List<Pago> pagos = pagoRepository.findByCitaIdCita(idCita);
        for (Pago pago : pagos) {
            if (PAGO_PAGADO.equals(pago.getEstado())) {
                continue;
            }
            pago.setEstado(PAGO_CANCELADO);
            pagoRepository.save(pago);
            stripeService.expirarSesion(pago.getStripeSessionId());
        }
    }

    /** Resultado para la página de retorno tras salir de Stripe. */
    @Transactional
    public ResultadoRetorno procesarRetorno(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return new ResultadoRetorno("DESCONOCIDO", null,
                    "No se recibió la referencia del pago.");
        }

        try {
            Optional<Pago> procesado = procesarPagoExitoso(sessionId);
            if (procesado.isEmpty()) {
                return new ResultadoRetorno("DESCONOCIDO", null,
                        "No encontramos el pago indicado.");
            }

            Pago pago = procesado.get();
            if (PAGO_PAGADO.equals(pago.getEstado())) {
                return new ResultadoRetorno(PAGO_PAGADO, pago.getCita().getIdCita(),
                        "Pago confirmado. Tu cita quedó programada.");
            }
            return new ResultadoRetorno(PAGO_PENDIENTE, pago.getCita().getIdCita(),
                    "El pago está en proceso; la cita se confirmará en cuanto Stripe lo apruebe.");
        } catch (StripeException e) {
            return new ResultadoRetorno("DESCONOCIDO", null,
                    "No pudimos verificar el pago con Stripe en este momento.");
        }
    }

    private Pago confirmar(Pago pago, StripeService.SesionInfo sesion) {
        Cita cita = pago.getCita();
        boolean citaCancelada = CITA_CANCELADA.equals(cita.getEstado());

        pago.setEstado(PAGO_PAGADO);
        pago.setFechaPago(LocalDateTime.now());
        if (sesion.paymentIntent() != null && !sesion.paymentIntent().isBlank()) {
            pago.setStripePaymentIntentId(sesion.paymentIntent());
        }
        pagoRepository.save(pago);

        if (!citaCancelada && CITA_PENDIENTE_PAGO.equals(cita.getEstado())) {
            cita.setEstado(CITA_PROGRAMADA);
            citaRepository.save(cita);
        }

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(cita.getMascota().getCliente());
        notificacion.setTipo("PAGO_CONFIRMADO");
        notificacion.setMensaje(citaCancelada
                ? "Su pago fue recibido pero la cita ya estaba cancelada; la clinica se comunicara para la devolucion"
                : String.format("Pago confirmado. Su cita para %s esta programada para el %s a las %s",
                        cita.getMascota().getNombre(),
                        cita.getFecha().format(FORMATO_FECHA),
                        cita.getHora()));
        notificacionService.save(notificacion);

        return pago;
    }

    private void liberarCita(Pago pago) {
        Cita cita = pago.getCita();
        if (CITA_PENDIENTE_PAGO.equals(cita.getEstado())) {
            cita.setEstado(CITA_CANCELADA);
            citaRepository.save(cita);
        }
    }

    /**
     * Libera los horarios reservados sin pago hace más de 30 minutos para que
     * vuelvan a estar disponibles.
     */
    private void liberarReservasVencidas(Long idVeterinario, LocalDate fecha, LocalTime hora) {
        if (idVeterinario == null || fecha == null || hora == null) {
            return;
        }

        LocalDateTime limite = LocalDateTime.now().minus(VIGENCIA_RESERVA);
        List<Cita> pendientes = citaRepository
                .findByVeterinarioIdUsuarioAndFechaAndHoraAndEstado(
                        idVeterinario, fecha, hora, CITA_PENDIENTE_PAGO);

        for (Cita pendiente : pendientes) {
            Pago pago = pagoRepository
                    .findFirstByCitaIdCitaOrderByFechaCreacionDesc(pendiente.getIdCita())
                    .orElse(null);

            boolean vencida = pago == null || pago.getFechaCreacion() == null
                    || pago.getFechaCreacion().isBefore(limite);
            if (!vencida) {
                continue;
            }

            pendiente.setEstado(CITA_CANCELADA);
            citaRepository.save(pendiente);

            if (pago != null && !PAGO_PAGADO.equals(pago.getEstado())) {
                pago.setEstado(PAGO_CANCELADO);
                pagoRepository.save(pago);
            }
        }
    }

    private boolean esDelCliente(Mascota mascota, Long idCliente) {
        if (idCliente == null || mascota.getCliente() == null) {
            return false;
        }
        /* Los administradores pueden gestionar citas de cualquier cliente. */
        return mascota.getCliente().getIdUsuario().equals(idCliente)
                || Autorizacion.esAdmin();
    }

    /** URL de pago devuelta al navegador para que se redirija a Stripe. */
    public record SesionPago(Long idPago, Long idCita, String sessionId, String url,
            BigDecimal monto, String moneda) {
    }

    /** Estado del pago al volver de Stripe, para la página de éxito. */
    public record ResultadoRetorno(String estado, Long idCita, String mensaje) {
    }
}

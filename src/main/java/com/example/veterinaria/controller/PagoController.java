package com.example.veterinaria.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import com.example.veterinaria.modelo.Cita;
import com.example.veterinaria.security.Autorizacion;
import com.example.veterinaria.service.PagoService;
import com.example.veterinaria.service.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Pasarela de pagos con Stripe.
 *
 *  - POST /api/pagos/checkout : crea la reserva y devuelve la URL de pago.
 *  - POST /api/pagos/webhook  : recibe los eventos de Stripe (firma verificada).
 */
@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private PagoService pagoService;

    @Autowired
    private StripeService stripeService;

    /** Crea la cita (pendiente de pago) y la sesión de Checkout de Stripe. */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody Cita cita, HttpServletRequest request) {
        Autorizacion.Usuario usuario = Autorizacion.actual();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "No autenticado"));
        }
        if (!stripeService.configurado()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("mensaje", "La pasarela de pago no está configurada"));
        }

        try {
            PagoService.SesionPago sesion = pagoService.crearCheckout(cita,
                    usuario.idUsuario(), urlPublica(request));
            return ResponseEntity.ok(Map.of(
                    "url", sesion.url(),
                    "sessionId", sesion.sessionId(),
                    "monto", sesion.monto(),
                    "moneda", sesion.moneda(),
                    "idCita", sesion.idCita()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("mensaje", mensaje(e)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", mensaje(e)));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("mensaje", "Stripe rechazó la operación, intente nuevamente"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "Ese horario ya está reservado"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", mensaje(e)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", mensaje(e)));
        }
    }

    /**
     * Webhook de Stripe: la confirmación definitiva del pago llega por acá (la
     * página de éxito también verifica, pero el webhook es quien garantiza que
     * ningún pago quede sin registrar aunque el navegador no vuelva).
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String firma) {

        if (!stripeService.webhookConfigurado()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("mensaje", "Webhook no configurado"));
        }
        if (firma == null || firma.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Firma ausente"));
        }

        try {
            stripeService.verificarEvento(payload, firma);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Firma inválida"));
        }

        String tipo = leer(payload, "/type");
        String sessionId = leer(payload, "/data/object/id");
        if (sessionId == null || sessionId.isBlank()) {
            return ResponseEntity.ok(Map.of("recibido", true));
        }

        try {
            switch (tipo == null ? "" : tipo) {
                case "checkout.session.completed" -> pagoService.procesarPagoExitoso(sessionId);
                case "checkout.session.expired",
                     "checkout.session.async_payment_failed" -> pagoService.cancelarPorSesion(sessionId);
                default -> { /* otros eventos se ignoran */ }
            }
        } catch (StripeException e) {
            /* 5xx para que Stripe reintente el evento más tarde. */
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "No se pudo verificar el pago"));
        }

        return ResponseEntity.ok(Map.of("recibido", true));
    }

    /**
     * URL pública de la app tal como la ve el navegador (esquema + host + puerto),
     * respetando los encabezados {@code X-Forwarded-Proto/Host} que pone el proxy
     * del hosting. Así los retornos de Stripe apuntan al dominio vigente aunque
     * cambie en cada deploy; {@code STRIPE_BASE_URL} sólo se usa si está definida.
     */
    private static String urlPublica(HttpServletRequest request) {
        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isBlank()) {
            host = request.getHeader("Host");
        }
        if (host == null || host.isBlank()) {
            host = request.getServerName()
                    + (request.getServerPort() == 80 || request.getServerPort() == 443
                            ? ""
                            : ":" + request.getServerPort());
        }
        String esquema = request.getHeader("X-Forwarded-Proto");
        if (esquema == null || esquema.isBlank()) {
            esquema = request.getScheme();
        }
        String contexto = request.getContextPath() == null ? "" : request.getContextPath();
        return esquema + "://" + host + contexto;
    }

    /** Map.of no admite valores nulos: se responde siempre un mensaje. */
    private static String mensaje(Exception e) {
        return e.getMessage() != null ? e.getMessage() : "No se pudo procesar la solicitud";
    }

    private static String leer(String json, String ruta) {
        try {
            JsonNode nodo = MAPPER.readTree(json).at(ruta);
            return nodo.isMissingNode() || nodo.isNull() ? null : nodo.asText();
        } catch (Exception e) {
            return null;
        }
    }
}

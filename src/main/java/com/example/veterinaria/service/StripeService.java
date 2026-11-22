package com.example.veterinaria.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

/**
 * Envoltorio del SDK de Stripe. Toda llamada a la API externa pasa por acá para
 * centralizar la clave y los parámetros, y para que PagoService se pueda probar
 * con un mock (sin red).
 */
@Service
public class StripeService {

    /** Moneda de la clínica: el Sol peruano (PEN). */
    public static final String MONEDA = "pen";

    @Value("${stripe.api-key:}")
    private String apiKey;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    /* Vacío por defecto: la URL pública se deduce de cada petición (así funciona
       aunque el dominio del deploy cambie); sólo se fija si se define la variable. */
    @Value("${stripe.base-url:}")
    private String baseUrl;

    /** true si hay clave configurada (sin clave no se puede cobrar). */
    public boolean configurado() {
        return apiKey != null && !apiKey.isBlank();
    }

    /** true si hay secreto del webhook para verificar la firma de los eventos. */
    public boolean webhookConfigurado() {
        return webhookSecret != null && !webhookSecret.isBlank();
    }

    /**
     * URL pública de la app para los retornos de Stripe, con este orden:
     * 1) lo que haya configurado en {@code STRIPE_BASE_URL} (dominio fijo),
     * 2) la URL deducida de la petición actual (deploy con dominio variable),
     * 3) localhost.
     */
    public String baseUrl(String urlDeLaPeticion) {
        if (baseUrl != null && !baseUrl.isBlank()) {
            return baseUrl.trim();
        }
        if (urlDeLaPeticion != null && !urlDeLaPeticion.isBlank()) {
            return urlDeLaPeticion.trim();
        }
        return "http://localhost:8080";
    }

    /** URL configurada o vacía (sin petición en contexto: por ejemplo en tests). */
    public String baseUrl() {
        return baseUrl(null);
    }

    /**
     * Crea una sesión de Checkout (página de pago alojada por Stripe) por el monto
     * indicado en su minor unit (céntimos) y devuelve id y URL de redirección.
     *
     * @param urlBase URL pública deducida de la petición; puede ser null.
     */
    public SesionCheckout crearSesionCheckout(BigDecimal monto, String nombreProducto,
            String descripcion, String correoCliente, Map<String, String> metadata)
            throws StripeException {
        return crearSesionCheckout(monto, nombreProducto, descripcion, correoCliente,
                metadata, null);
    }

    /** Igual que {@link #crearSesionCheckout} indicando la URL pública de retorno. */
    public SesionCheckout crearSesionCheckout(BigDecimal monto, String nombreProducto,
            String descripcion, String correoCliente, Map<String, String> metadata,
            String urlBase) throws StripeException {

        String base = baseUrl(urlBase);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCurrency(MONEDA)
                .setSuccessUrl(base + "/pago/exito?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(base + "/pago/cancelado?session_id={CHECKOUT_SESSION_ID}")
                .setCustomerEmail(correoCliente == null || correoCliente.isBlank() ? null : correoCliente)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(MONEDA)
                                .setUnitAmount(montoCents(monto))
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(nombreProducto)
                                        .setDescription(descripcion)
                                        .build())
                                .build())
                        .build())
                .putAllMetadata(metadata)
                .build();

        Session session = Session.create(params, opciones());
        return new SesionCheckout(session.getId(), session.getUrl());
    }

    /**
     * Lee una sesión de Stripe. {@code estadoPago} es "paid", "unpaid" o "no_payment_required".
     */
    public SesionInfo obtenerSesion(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId, opciones());
        return new SesionInfo(session.getId(), session.getPaymentStatus(),
                session.getPaymentIntent(), session.getMetadata());
    }

    /** Cierra una sesión que ya no se va a usar (mejor esfuerzo, sin excepciones). */
    public void expirarSesion(String sessionId) {
        if (!configurado() || sessionId == null || sessionId.isBlank()) {
            return;
        }
        try {
            Session.retrieve(sessionId, opciones()).expire(opciones());
        } catch (StripeException | RuntimeException e) {
            // La sesión ya expiró o no existe: no hay nada que cerrar.
        }
    }

    /** Verifica la firma del webhook y devuelve el evento; lanza SignatureVerificationException. */
    public Event verificarEvento(String payload, String firma) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, firma, webhookSecret);
    }

    /** Céntimos (minor unit de PEN) redondeando de forma segura. */
    public static long montoCents(BigDecimal monto) {
        return monto.multiply(BigDecimal.valueOf(100))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .longValueExact();
    }

    private RequestOptions opciones() {
        return RequestOptions.builder().setApiKey(apiKey).build();
    }

    public record SesionCheckout(String id, String url) {
    }

    public record SesionInfo(String id, String estadoPago, String paymentIntent,
            Map<String, String> metadata) {
    }
}

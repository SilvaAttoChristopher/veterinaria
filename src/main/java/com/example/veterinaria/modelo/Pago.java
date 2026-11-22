package com.example.veterinaria.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro de un pago contra la pasarela de Stripe. Cada cita que se agrega con
 * pago asociado tiene al menos un Pago (PENDIENTE hasta que Stripe confirma).
 */
@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cita", nullable = false)
    private Cita cita;

    @Column(nullable = false)
    private BigDecimal monto = BigDecimal.ZERO;

    /** ISO 4217 en minúsculas, como lo espera Stripe ("pen"). */
    @Size(max = 3)
    @Column(nullable = false, length = 3)
    private String moneda = "pen";

    /** id de la sesión de Checkout de Stripe (cs_...). */
    @Size(max = 255)
    @Column(unique = true, length = 255)
    private String stripeSessionId;

    /** id del PaymentIntent asociado (pi_...), disponible al confirmar. */
    @Size(max = 255)
    @Column(length = 255)
    private String stripePaymentIntentId;

    /** PENDIENTE, PAGADO, CANCELADO o FALLIDO. */
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private LocalDateTime fechaPago;

    public Pago() {}

    public Long getIdPago() { return idPago; }
    public void setIdPago(Long idPago) { this.idPago = idPago; }

    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public String getStripeSessionId() { return stripeSessionId; }
    public void setStripeSessionId(String stripeSessionId) { this.stripeSessionId = stripeSessionId; }

    public String getStripePaymentIntentId() { return stripePaymentIntentId; }
    public void setStripePaymentIntentId(String stripePaymentIntentId) { this.stripePaymentIntentId = stripePaymentIntentId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
}

package com.example.veterinaria.modelo;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vacunacion")
public class Vacunacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVacunacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cartilla", nullable = false)
    private Cartilla cartilla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vacuna", nullable = false)
    private Vacuna vacuna;

    @Column(nullable = false)
    private LocalDate fechaAplicacion;

    private LocalDate proximaDosis;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    public Vacunacion() {}

    public Long getIdVacunacion() { return idVacunacion; }
    public void setIdVacunacion(Long idVacunacion) { this.idVacunacion = idVacunacion; }

    public Cartilla getCartilla() { return cartilla; }
    public void setCartilla(Cartilla cartilla) { this.cartilla = cartilla; }

    public Vacuna getVacuna() { return vacuna; }
    public void setVacuna(Vacuna vacuna) { this.vacuna = vacuna; }

    public LocalDate getFechaAplicacion() { return fechaAplicacion; }
    public void setFechaAplicacion(LocalDate fechaAplicacion) { this.fechaAplicacion = fechaAplicacion; }

    public LocalDate getProximaDosis() { return proximaDosis; }
    public void setProximaDosis(LocalDate proximaDosis) { this.proximaDosis = proximaDosis; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}

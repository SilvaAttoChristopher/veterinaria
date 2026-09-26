package com.example.veterinaria.modelo;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cartilla")
public class Cartilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCartilla;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mascota", nullable = false, unique = true)
    private Mascota mascota;

    @Column(nullable = false)
    private LocalDate fechaCreacion = LocalDate.now();

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    public Cartilla() {}

    public Cartilla(Mascota mascota) {
        this.mascota = mascota;
    }

    public Long getIdCartilla() { return idCartilla; }
    public void setIdCartilla(Long idCartilla) { this.idCartilla = idCartilla; }

    public Mascota getMascota() { return mascota; }
    public void setMascota(Mascota mascota) { this.mascota = mascota; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}

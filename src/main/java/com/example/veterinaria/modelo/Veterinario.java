package com.example.veterinaria.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "veterinario")
@PrimaryKeyJoinColumn(name = "id_veterinario")
public class Veterinario extends Usuario {

    @Size(max = 100)
    @Column(length = 100)
    private String especialidad;

    public Veterinario() {}

    public Veterinario(String username, String password, String nombres, String apellidos, String especialidad, Rol rol) {
        super(username, password, nombres, apellidos, rol);
        this.especialidad = especialidad;
    }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
}

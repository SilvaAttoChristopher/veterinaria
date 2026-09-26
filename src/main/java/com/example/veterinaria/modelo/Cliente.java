package com.example.veterinaria.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "cliente")
@PrimaryKeyJoinColumn(name = "id_cliente")
public class Cliente extends Usuario {

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    public Cliente() {}

    public Cliente(String username, String password, String nombres, String apellidos, String dni, Rol rol) {
        super(username, password, nombres, apellidos, rol);
        this.dni = dni;
    }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
}

package com.example.veterinaria.modelo;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class RolPermisoId implements Serializable {

    @Column(name = "id_rol")
    private Long idRol;

    @Column(name = "id_permiso")
    private Long idPermiso;

    public RolPermisoId() {}

    public RolPermisoId(Long idRol, Long idPermiso) {
        this.idRol = idRol;
        this.idPermiso = idPermiso;
    }

    public Long getIdRol() { return idRol; }
    public void setIdRol(Long idRol) { this.idRol = idRol; }

    public Long getIdPermiso() { return idPermiso; }
    public void setIdPermiso(Long idPermiso) { this.idPermiso = idPermiso; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolPermisoId that)) return false;
        return Objects.equals(idRol, that.idRol) && Objects.equals(idPermiso, that.idPermiso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRol, idPermiso);
    }
}

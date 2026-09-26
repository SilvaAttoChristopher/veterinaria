package com.example.veterinaria.modelo;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "rol_permiso")
public class RolPermiso {

    @EmbeddedId
    private RolPermisoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idRol")
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idPermiso")
    @JoinColumn(name = "id_permiso", nullable = false)
    private Permiso permiso;

    public RolPermiso() {}

    public RolPermiso(Rol rol, Permiso permiso) {
        this.rol = rol;
        this.permiso = permiso;
        this.id = new RolPermisoId(rol.getIdRol(), permiso.getIdPermiso());
    }

    public RolPermisoId getId() { return id; }
    public void setId(RolPermisoId id) { this.id = id; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public Permiso getPermiso() { return permiso; }
    public void setPermiso(Permiso permiso) { this.permiso = permiso; }
}

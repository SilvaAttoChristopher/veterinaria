package com.example.veterinaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.veterinaria.modelo.RolPermiso;
import com.example.veterinaria.modelo.RolPermisoId;

@Repository
public interface RolPermisoRepository extends JpaRepository<RolPermiso, RolPermisoId> {
}

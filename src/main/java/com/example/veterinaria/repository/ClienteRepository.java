package com.example.veterinaria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.veterinaria.modelo.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByDni(String dni);

    @Query("SELECT c FROM Cliente c WHERE c.nombres LIKE %:criterio% OR c.apellidos LIKE %:criterio% OR c.dni LIKE %:criterio% OR c.telefono LIKE %:criterio%")
    List<Cliente> buscarPorCriterio(@Param("criterio") String criterio);
}

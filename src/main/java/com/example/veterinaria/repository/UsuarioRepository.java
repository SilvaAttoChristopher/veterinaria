package com.example.veterinaria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.veterinaria.modelo.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = {"rol"})
    Optional<Usuario> findByUsername(String username);

    List<Usuario> findByEstado(String estado);

    boolean existsByUsername(String username);

    List<Usuario> findByDni(String dni);

    /** Usuarios de un rol concreto, por nombre (CLIENTE, VETERINARIO, ADMIN). */
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = :nombre ORDER BY u.apellidos, u.nombres")
    List<Usuario> findByRolNombre(@Param("nombre") String nombre);

    /** Búsqueda de clientes por nombre, apellido, dni o teléfono. */
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = 'CLIENTE' AND ("
            + "u.nombres LIKE %:criterio% OR u.apellidos LIKE %:criterio% "
            + "OR u.dni LIKE %:criterio% OR u.telefono LIKE %:criterio%)")
    List<Usuario> buscarClientesPorCriterio(@Param("criterio") String criterio);

    /** Veterinarios activos, para los selectores de agendar cita. */
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = 'VETERINARIO' "
            + "AND u.estado = 'ACTIVO' ORDER BY u.apellidos, u.nombres")
    List<Usuario> findVeterinariosActivos();
}

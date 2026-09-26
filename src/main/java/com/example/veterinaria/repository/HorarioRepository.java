package com.example.veterinaria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.veterinaria.modelo.Horario;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {

    List<Horario> findByVeterinarioIdUsuario(Long idUsuario);

    List<Horario> findByVeterinarioIdUsuarioAndDiaSemana(Long idUsuario, Short diaSemana);
}

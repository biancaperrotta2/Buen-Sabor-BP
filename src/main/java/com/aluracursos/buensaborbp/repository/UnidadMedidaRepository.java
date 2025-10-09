package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.UnidadMedida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida,Long> {
    Optional<UnidadMedida> findByDenominacion (String denominacion);
}

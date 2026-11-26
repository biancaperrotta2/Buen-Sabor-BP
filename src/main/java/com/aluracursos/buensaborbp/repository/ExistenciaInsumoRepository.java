package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.ExistenciaInsumo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExistenciaInsumoRepository extends JpaRepository<ExistenciaInsumo, Long> {

    List<ExistenciaInsumo> findByInsumo_IdOrderByFechaCompraDescIdDesc(Long insumoId);

    // La compra más reciente: su costo pasa a ser el precio de costo del insumo.
    Optional<ExistenciaInsumo> findFirstByInsumo_IdOrderByFechaCompraDescIdDesc(Long insumoId);
}

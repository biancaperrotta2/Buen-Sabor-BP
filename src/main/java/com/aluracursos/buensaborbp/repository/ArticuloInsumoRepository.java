package com.aluracursos.buensaborbp.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aluracursos.buensaborbp.entity.ArticuloInsumo;

@Repository
public interface ArticuloInsumoRepository extends BaseRepository<ArticuloInsumo, Long> {
    List<ArticuloInsumo> findByActivoTrue();

    // Los que se venden directo en el menú (esVendible = !esParaElaborar).
    List<ArticuloInsumo> findByActivoTrueAndEsVendibleTrue();

    // Consulta para el Dashboard de Admin (Sprint 6)
    @Query("SELECT i FROM ArticuloInsumo i WHERE i.stockActual <= i.stockMinimo AND i.activo = true")
    List<ArticuloInsumo> findBajoStock();

    // Tabla de admin "Insumos": búsqueda libre por nombre.
    @Query("SELECT i FROM ArticuloInsumo i WHERE :busqueda = '' " +
            "OR LOWER(i.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<ArticuloInsumo> buscar(@Param("busqueda") String busqueda, Pageable pageable);
}
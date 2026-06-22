package com.aluracursos.buensaborbp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aluracursos.buensaborbp.entity.ArticuloInsumo;

@Repository
public interface ArticuloInsumoRepository extends BaseRepository<ArticuloInsumo, Long> {
    List<ArticuloInsumo> findByActivoTrue();
    
    // Consulta para el Dashboard de Admin (Sprint 6)
    @Query("SELECT i FROM ArticuloInsumo i WHERE i.stockActual <= i.stockMinimo AND i.activo = true")
    List<ArticuloInsumo> findBajoStock();
}
package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Promocion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PromocionRepository extends BaseRepository<Promocion, Long> {
    
    // Para el catálogo del Cliente (Filtra por vigencia y habilitación)
    @Query("SELECT p FROM Promocion p WHERE p.habilitado = true AND " +
           "(CURRENT_DATE BETWEEN p.fechaDesde AND p.fechaHasta) AND " +
           "(CURRENT_TIME BETWEEN p.horaDesde AND p.horaHasta)")
    List<Promocion> findVigentes();

    // Para el Admin: Búsqueda por nombre
    List<Promocion> findByDenominacionContainingIgnoreCase(String denominacion);
}
package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Promocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PromocionRepository extends BaseRepository<Promocion, Long> {

    // Para el catálogo del Cliente (Filtra por vigencia y habilitación)
    @Query("SELECT p FROM Promocion p WHERE p.activo = true AND " +
           "(CURRENT_DATE BETWEEN p.fechaDesde AND p.fechaHasta) AND " +
           "(CURRENT_TIME BETWEEN p.horaDesde AND p.horaHasta)")
    List<Promocion> findVigentes();

    // Promociones que incluyen un artículo directamente (no vía la receta de un producto).
    @Query("SELECT DISTINCT d.articuloPromocion FROM promocion_detalle d WHERE d.articulos.id = :articuloId")
    List<Promocion> findQueIncluyenArticulo(@Param("articuloId") Long articuloId);

    // Para el Admin: Búsqueda por nombre
    List<Promocion> findByNombreContainingIgnoreCase(String nombre);

    // Para la tabla del Admin: búsqueda + paginado + orden, activas e inactivas por igual.
    @Query("SELECT p FROM Promocion p WHERE :busqueda = '' OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Promocion> buscar(@Param("busqueda") String busqueda, Pageable pageable);
}
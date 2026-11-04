package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria,Long> {
    // Solo activas: una subcategoría desactivada no tiene que seguir apareciendo en los formularios.
    List<Categoria> findByCategoriaPadreIdAndActivoTrue(Long categoriaPadreId);
    Optional<Categoria> findByDenominacion (String denominacion);
    List<Categoria> findByCategoriaPadreIsNullAndActivoTrue();

    // Tabla de admin "Categorías": mismo alcance que mostrarCategorias() (principales,
    // activas), pero con búsqueda + paginado + orden. No reemplaza a /categoria/all, que
    // sigue sin paginar porque lo consumen los <select> de categoría de los formularios de
    // Insumo/Categoría (necesitan la lista completa, no una página).
    @Query("SELECT c FROM Categoria c WHERE c.categoriaPadre IS NULL AND c.activo = true " +
            "AND (:busqueda = '' OR LOWER(c.denominacion) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    Page<Categoria> buscarPrincipales(@Param("busqueda") String busqueda, Pageable pageable);
}

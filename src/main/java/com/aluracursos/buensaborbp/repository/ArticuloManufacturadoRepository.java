package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticuloManufacturadoRepository extends JpaRepository <ArticuloManufacturado, Long> {

    // El catálogo (público o de admin) muestra platos finales, nunca semielaborados
    // (esos tienen su propia pantalla — ver findAllByEsSemielaboradoTrue).
    Page<ArticuloManufacturado> findAllByActivoTrueAndEsSemielaboradoFalse(PageRequest of);

    Page<ArticuloManufacturado> findAllByEsSemielaboradoFalse(PageRequest of);

    Page<ArticuloManufacturado> findAllByEsSemielaboradoTrue(PageRequest of);

    // Para el gráfico de Stock del dashboard de admin: a diferencia de un plato final
    // (cuyo propio stockActual/Minimo/Maximo no se usa, ver CLAUDE.md), un semielaborado
    // sí lleva su propio stock igual que un ArticuloInsumo.
    List<ArticuloManufacturado> findByActivoTrueAndEsSemielaboradoTrue();

    // Tabla de admin "Productos": búsqueda + paginado + orden real, a diferencia de
    // getAll()/getActivos() (page 0 tamaño 20 fijo, usados por el catálogo público). No
    // reemplaza esos: el menú público sigue llamando a /articulo-manufacturado tal cual.
    @Query("SELECT m FROM ArticuloManufacturado m WHERE m.esSemielaborado = false " +
            "AND (:busqueda = '' OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    Page<ArticuloManufacturado> buscarProductos(@Param("busqueda") String busqueda, Pageable pageable);

    // Tabla de admin/cocinero de semielaborados (búsqueda + paginado + orden).
    @Query("SELECT m FROM ArticuloManufacturado m WHERE m.esSemielaborado = true " +
            "AND (:busqueda = '' OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    Page<ArticuloManufacturado> buscarSemielaborados(@Param("busqueda") String busqueda, Pageable pageable);
}

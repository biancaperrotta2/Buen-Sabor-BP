package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.ArticuloManufacturadoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticuloManufacturadoDetalleRepository extends JpaRepository<ArticuloManufacturadoDetalle, Long> {

    // Recetas que usan un artículo como ingrediente (ver ArticuloInsumoServiceImpl.update:
    // al cambiar la unidad del insumo, estas cantidades se convierten).
    List<ArticuloManufacturadoDetalle> findByComponente_Id(Long componenteId);
}

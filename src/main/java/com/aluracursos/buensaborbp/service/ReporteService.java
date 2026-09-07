package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.Agrupacion;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoBaseResponse;
import com.aluracursos.buensaborbp.dto.IngresoPeriodoResponse;
import com.aluracursos.buensaborbp.dto.OrdenesPorClienteResponse;
import com.aluracursos.buensaborbp.dto.RankingProductoResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReporteService {
    // Cantidad vendida por artículo, entre las dos fechas (inclusive), excluyendo pedidos CANCELADO.
    List<RankingProductoResponse> rankingProductos(LocalDate desde, LocalDate hasta);

    // Recaudación real (pedidos ENTREGADO) agrupada por día o por mes, entre las dos fechas.
    List<IngresoPeriodoResponse> ingresosPorPeriodo(LocalDate desde, LocalDate hasta, Agrupacion agrupacion);

    // Cantidad de pedidos por cliente, entre las dos fechas, excluyendo pedidos CANCELADO.
    List<OrdenesPorClienteResponse> ordenesPorCliente(LocalDate desde, LocalDate hasta);

    // Insumos activos con stockActual <= stockMinimo.
    List<ArticuloInsumoBaseResponse> stockBajoMinimo();
}

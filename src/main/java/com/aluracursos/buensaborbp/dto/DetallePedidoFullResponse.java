package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

public record DetallePedidoFullResponse(
    ArticuloManufacturadoFullResponse articuloManufacturado,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal,
    // Nombre de lo vendido sea plato, promoción o insumo (articuloManufacturado solo viene
    // cuando es un plato; sin esto "Mis pedidos" mostraba "Producto" para el resto).
    String nombreArticulo
) { }

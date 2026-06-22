package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

public record DetallePedidoFullResponse(
    ArticuloManufacturadoFullResponse articuloManufacturado,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) { }

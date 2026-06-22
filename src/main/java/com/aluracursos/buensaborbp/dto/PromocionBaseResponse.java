package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

public record PromocionBaseResponse(
    Long id,
    String nombre, // nombrePromocion
    BigDecimal precioVenta, // precioPromocional
    String descripcionDescuento,
    String urlImagen 
) {}

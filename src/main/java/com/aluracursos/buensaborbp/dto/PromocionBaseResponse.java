package com.aluracursos.buensaborbp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromocionBaseResponse {
    private String nombrePromocion;
    private BigDecimal precioPromocional;
    private String descripcionPromocion;
    private ImagenPromocionResponse imagenPromocion;
}

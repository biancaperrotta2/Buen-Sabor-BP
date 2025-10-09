package com.aluracursos.buensaborbp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticuloManufacturadoBaseResponse {
    private Long id;
    private String denominacion;
    private String descripcion;
    private BigDecimal precioVenta;
    private String imagenUrl;
}


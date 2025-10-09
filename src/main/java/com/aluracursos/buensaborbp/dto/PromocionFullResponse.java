package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.TipoPromocion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromocionFullResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private TipoPromocion tipo;
    private BigDecimal precioPromocional;
    private ImagenPromocionResponse imagen;
    private List<ArticuloManufacturadoBaseResponse> articulosIncluidos;
}

package com.aluracursos.buensaborbp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticuloManufacturadoFullResponse {
    private Long id;
    private String denominacion;
    private String descripcion;
    private Integer tiempoEstimado;
    private Integer stockActual;
    private BigDecimal precioVenta;
    private BigDecimal precioCosto; // calculado en el back
    private BigDecimal ganancia;    // también calculado en el back
    private String preparacion;

    private CategoriaFullResponse categoria;
    private List<ImagenArticuloResponse> imagenes;
    private List<ArticuloManufacturadoDetalleResponse> detalles;
}


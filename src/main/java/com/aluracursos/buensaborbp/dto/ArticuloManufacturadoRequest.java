package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.entity.ImagenArticulo;
import com.aluracursos.buensaborbp.entity.UnidadMedida;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloManufacturadoRequest {
    private String denominacion;
    private Integer stockMinimo;
    private Integer stockMaximo;
    private String descripcion;
    private BigDecimal precioVenta;
    private Integer tiempoEstimado; // calculado en back pero se muestra
    private String preparacion; // dejar?
    private CategoriaRequest categoria;
    private List<ImagenArticuloRequest> imagenes;
    private List<ArticuloManufacturadoDetalleRequest> articuloManufacturadoDetalles;
}

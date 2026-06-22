package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;
import java.util.List;

public record ArticuloInsumoRequest(
    String denominacion,
    BigDecimal precioCompra,
    Integer stockMinimo,
    Integer stockMaximo,
    Boolean esParaElaborar,
    BigDecimal precioVenta,
    List<ImagenArticuloRequest> imagenes,
    UnidadMedidaRequest unidadMedida,
    CategoriaRequest categoria
) {}

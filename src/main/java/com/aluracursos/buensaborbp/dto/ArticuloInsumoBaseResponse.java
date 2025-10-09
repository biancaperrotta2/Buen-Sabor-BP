package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Categoria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloInsumoBaseResponse {
    private Long id;
    private String denominacion;
    private BigDecimal precioCompra;
    private CategoriaBaseResponse categoria;
    private Boolean activo;
    private UnidadMedidaResponse unidadMedida;
}

package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.entity.UnidadMedida;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloInsumoFullResponse {
    private Long id;
    private String denominacion;
    private Double precioCompra;
    private Double precioVenta;
    private Double stockActual;
    private Double stockMaximo;
    private Boolean esParaElaborar;
    private Double stockMinimo;
    private Boolean activo;
    private CategoriaFullResponse categoria;
    private UnidadMedidaResponse unidadMedida;
}

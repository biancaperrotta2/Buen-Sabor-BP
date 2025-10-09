package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticuloManufacturadoDetalleResponse {
    private Integer cantidad;
    private ArticuloInsumo articuloInsumo;
}

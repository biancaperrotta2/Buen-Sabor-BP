package com.aluracursos.buensaborbp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticuloManufacturadoDetalleRequest{
        private Integer cantidad;
        private ArticuloInsumoBaseResponse articuloInsumo;
}

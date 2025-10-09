package com.aluracursos.buensaborbp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PedidoRequest {
    private String tipoEntrega;
    private String metodoPago;
    private String aclaracion;
    private List<DetallePedidoRequest> detalles;
}

package com.aluracursos.buensaborbp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PedidoFullResponse {
    private Long id;
    private String estado;
    private LocalDateTime fechaHora;
    private String tipoEntrega;
    private String aclaracion;
    private String metodoPago;
    private Double subtotal;
    private Double costoEnvio;
    private Double descuento;
    private Double total;
    private List<DetallePedidoFullResponse> detalles;
}

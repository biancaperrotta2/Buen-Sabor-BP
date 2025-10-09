package com.aluracursos.buensaborbp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PedidoBaseResponse {
    private Long id;
    private String estado;
    private LocalDateTime fechaHora;
    private String tipoEntrega;
    private List<DetallePedidoFullResponse> detalles;
}

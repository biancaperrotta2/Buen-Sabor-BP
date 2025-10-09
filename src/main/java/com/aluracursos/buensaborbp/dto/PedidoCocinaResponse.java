package com.aluracursos.buensaborbp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PedidoCocinaResponse {
    private Long idPedido;               // número de orden
    private String clienteNombre;  // nombre y apellido
    private String tiempoEstimado;
    private String tipoEntrega;
    private String estado;         // "pendiente", "en proceso", "disponible"
}


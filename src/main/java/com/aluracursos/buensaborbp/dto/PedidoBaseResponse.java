package com.aluracursos.buensaborbp.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;

public record PedidoBaseResponse(
    Long id,
    Estado estadoPedido,
    LocalTime horaEstimadaFinalizacion,
    LocalDate fechaPedido,
    TipoEnvio tipoEnvio,
    List<DetallePedidoFullResponse> detalles
) {}

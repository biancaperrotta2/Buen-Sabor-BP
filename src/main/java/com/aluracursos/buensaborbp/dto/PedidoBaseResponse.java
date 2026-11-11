package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.EstadoPago;
import com.aluracursos.buensaborbp.entity.Enums.FormaPago;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;

public record PedidoBaseResponse(
    Long id,
    Estado estadoPedido,
    EstadoPago estadoPago,
    LocalTime horaEstimadaFinalizacion,
    LocalDate fechaPedido,
    TipoEnvio tipoEnvio,
    FormaPago formaPago,
    String domicilioSnapshot,
    BigDecimal total,
    Long idFactura,
    String motivoCancelacion,
    Boolean reintegroPendiente,
    List<DetallePedidoFullResponse> detalles
) {}

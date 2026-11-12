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
    // Para el diálogo de detalle de "Mis órdenes" (mismos nombres que en Pedido: se mapean solos).
    BigDecimal gastosEnvio,
    BigDecimal descuento,
    String aclaraciones,
    Long idFactura,
    String motivoCancelacion,
    Boolean reintegroPendiente,
    List<DetallePedidoFullResponse> detalles
) {}

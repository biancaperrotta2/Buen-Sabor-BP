package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.EstadoPago;
import com.aluracursos.buensaborbp.entity.Enums.FormaPago;

import java.math.BigDecimal;
import java.time.LocalTime;

// Vista para el repartidor: lo que necesita para hacer la entrega (a dónde ir, cuánto
// cobrar si es en efectivo), sin el detalle de artículos que ya usa la pantalla de cocina.
public record PedidoEntregaResponse(
    Long id,
    Estado estadoPedido,
    String domicilioSnapshot,
    String aclaracionDomicilio,
    BigDecimal total,
    FormaPago formaPago,
    EstadoPago estadoPago,
    LocalTime horaEstimadaFinalizacion
) {}

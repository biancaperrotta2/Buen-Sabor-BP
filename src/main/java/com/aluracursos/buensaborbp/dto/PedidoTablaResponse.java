package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.EstadoPago;
import com.aluracursos.buensaborbp.entity.Enums.FormaPago;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

// Fila de las tablas de Órdenes (cajero) y Cocina (figuras 32 y 36): Orden, Cliente, Tiempo,
// Envío y Estado, más lo que necesita Órdenes para avisar pagos pendientes.
public record PedidoTablaResponse(
    Long id,
    LocalDate fechaPedido,
    LocalTime horaEstimadaFinalizacion,
    String cliente,
    TipoEnvio tipoEnvio,
    Estado estado,
    EstadoPago estadoPago,
    FormaPago formaPago,
    BigDecimal total
) {}

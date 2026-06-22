package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.EstadoPago;
import com.aluracursos.buensaborbp.entity.Enums.FormaPago;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;

public record PedidoFullResponse(
    Long id,
    Estado estadoPedido,
    EstadoPago estadoPago,
    LocalDate fechaPedido,
    TipoEnvio tipoEnvio,
    String domicilioSnapshot,
    String aclaracionDomicilio,
    LocalTime horaEstimadaFinalizacion,
    FormaPago formaPago,
    BigDecimal costoTotal,
    BigDecimal gastosEnvio,
    Double descuento,
    BigDecimal total,
    ClienteBaseResponse cliente, 
    List<DetallePedidoFullResponse> detallePedidos
) {}


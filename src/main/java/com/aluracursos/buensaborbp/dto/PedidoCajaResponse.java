package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.EstadoPago;
import com.aluracursos.buensaborbp.entity.Enums.FormaPago;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;

// Vista para el cajero: pedidos por aceptar (PENDIENTE) y pedidos listos para
// entregar/cobrar en el mostrador (PREPARADO + TAKE_AWAY).
public record PedidoCajaResponse(
    Long id,
    ClienteBaseResponse cliente,
    Estado estadoPedido,
    TipoEnvio tipoEnvio,
    FormaPago formaPago,
    EstadoPago estadoPago,
    BigDecimal total,
    LocalTime horaEstimadaFinalizacion
) {}

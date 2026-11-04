package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.EstadoPago;

// Resultado de preguntarle a Mercado Pago por el pago de un pedido. pagoEncontrado = false
// significa que MP todavía no registra ningún pago para ese pedido (estadoPago queda como estaba).
public record VerificacionPagoResponse(
        Long idPedido,
        EstadoPago estadoPago,
        boolean pagoEncontrado
) {}

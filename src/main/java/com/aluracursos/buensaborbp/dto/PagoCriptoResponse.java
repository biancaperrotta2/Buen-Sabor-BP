package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

// Link a la página de pago de CoinGate y el monto en dólares que se cobra (el sandbox de CoinGate
// no acepta pesos: el total se convierte con la cotización fija de cripto.cotizacion-usd).
public record PagoCriptoResponse(
    Long idPedido,
    String urlPago,
    BigDecimal montoUsd,
    BigDecimal cotizacionUsd
) {}

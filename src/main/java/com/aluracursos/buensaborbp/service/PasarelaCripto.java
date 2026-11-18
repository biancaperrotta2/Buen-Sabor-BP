package com.aluracursos.buensaborbp.service;

import java.util.Map;

// Pasarela de pago con criptomonedas. Hay dos: la simulada (por defecto, ver
// PasarelaCriptoSimulada) y CoinGate (CoinGatePagos). Se elige con cripto.proveedor; el resto
// del circuito (PedidoServiceImpl) no sabe cuál es.
public interface PasarelaCripto {

    // Lo que nos interesa de una orden de pago. status usa los nombres de CoinGate: new, pending,
    // confirming, paid, invalid, expired, canceled, refunded, partially_refunded.
    record OrdenCripto(Long id, String status, String orderId, String paymentUrl) {}

    // campos: order_id, price_amount, price_currency, title, description, callback_url,
    // success_url, cancel_url, token (mismo formato que la API de CoinGate).
    OrdenCripto crearOrden(Map<String, String> campos);

    // Lanza PagoInexistenteException si la orden no existe.
    OrdenCripto obtenerOrden(Long idOrden);
}

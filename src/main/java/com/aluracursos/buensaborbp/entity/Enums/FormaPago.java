package com.aluracursos.buensaborbp.entity.Enums;

// "Pagar con tarjeta" en la propuesta real del proyecto redirige al Checkout de Mercado
// Pago (es MP quien procesa la tarjeta) — no es una pasarela de tarjeta aparte, por eso no
// hay un valor TARJETA_* propio.
public enum FormaPago {
    EFECTIVO,
    MERCADO_PAGO
}

package com.aluracursos.buensaborbp.entity.Enums;

public enum EstadoPago {
    PENDIENTE,   // Todavía no pagó (ej: eligió efectivo y no llegó a la casa)
    PAGADO,      // La plata ya entró (ej: Mercado Pago confirmó o el delivery cobró)
    RECHAZADO    // La tarjeta no pasó o hubo un problema con la transacción
}
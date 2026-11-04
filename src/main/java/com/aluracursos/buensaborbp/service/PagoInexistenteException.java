package com.aluracursos.buensaborbp.service;

// Mercado Pago no tiene ningún pago con ese id (por ejemplo, el aviso de "Simular notificación"
// del panel, que manda un id inventado).
public class PagoInexistenteException extends RuntimeException {
    public PagoInexistenteException(Long paymentId) {
        super("Mercado Pago no tiene ningún pago con id " + paymentId);
    }
}

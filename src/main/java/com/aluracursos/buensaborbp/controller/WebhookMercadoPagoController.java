package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.service.PedidoService;
import com.mercadopago.exceptions.MPInvalidWebhookSignatureException;
import com.mercadopago.webhook.WebhookSignatureValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Endpoint público (ver SecurityConfig): a este lo llama Mercado Pago directamente, sin JWT
// nuestro. La autenticidad se garantiza validando la firma x-signature, no con Spring Security.
@RestController
@RequestMapping("/webhooks/mercadopago")
@RequiredArgsConstructor
public class WebhookMercadoPagoController {

    private static final Logger log = LoggerFactory.getLogger(WebhookMercadoPagoController.class);

    private final PedidoService pedidoService;

    @Value("${mercadopago.webhook-secret:}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<Void> recibirNotificacion(
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "data.id", required = false) String dataId,
            @RequestHeader(name = "x-signature", required = false) String xSignature,
            @RequestHeader(name = "x-request-id", required = false) String xRequestId) {

        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Llegó una notificación de Mercado Pago pero mercadopago.webhook-secret no está configurado");
            return ResponseEntity.status(503).build();
        }

        try {
            WebhookSignatureValidator.validate(xSignature, xRequestId, dataId, webhookSecret);
        } catch (MPInvalidWebhookSignatureException e) {
            // No se expone el motivo puntual en la respuesta (recomendación del propio SDK):
            // evita darle pistas a quien intente falsificar notificaciones.
            log.warn("Firma inválida en webhook de Mercado Pago (x-request-id={})", xRequestId);
            return ResponseEntity.status(401).build();
        }

        // Solo nos interesan las notificaciones de pagos; el resto se reconoce con 200 y se ignora.
        if ("payment".equals(type) && dataId != null) {
            pedidoService.procesarNotificacionPago(Long.valueOf(dataId));
        }

        return ResponseEntity.ok().build();
    }
}

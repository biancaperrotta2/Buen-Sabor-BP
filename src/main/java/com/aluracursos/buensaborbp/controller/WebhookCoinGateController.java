package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.service.PagoInexistenteException;
import com.aluracursos.buensaborbp.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Endpoint público (ver SecurityConfig): lo llama CoinGate cuando cambia el estado de una orden.
// CoinGate no firma el aviso: se valida con el token que mandamos al crear la orden y, además,
// el servicio vuelve a consultar la orden a CoinGate antes de cambiar el pedido.
// El formato (JSON o formulario) se elige por app en el panel de CoinGate: se aceptan los dos.
@RestController
@RequestMapping("/webhooks/coingate")
@RequiredArgsConstructor
public class WebhookCoinGateController {

    private static final Logger log = LoggerFactory.getLogger(WebhookCoinGateController.class);

    private final PedidoService pedidoService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> recibirJson(@RequestBody Map<String, Object> aviso) {
        return procesar(texto(aviso.get("id")), texto(aviso.get("order_id")), texto(aviso.get("token")), texto(aviso.get("status")));
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> recibirFormulario(@RequestParam Map<String, String> aviso) {
        return procesar(aviso.get("id"), aviso.get("order_id"), aviso.get("token"), aviso.get("status"));
    }

    private ResponseEntity<Void> procesar(String id, String orderId, String token, String status) {
        log.info("Aviso de CoinGate: orden {} del pedido {} -> {}", id, orderId, status);
        try {
            pedidoService.procesarAvisoCripto(id == null ? null : Long.valueOf(id), orderId, token);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            log.warn("Aviso de CoinGate con token inválido (orden {}, pedido {})", id, orderId);
            return ResponseEntity.status(401).build();
        } catch (PagoInexistenteException e) {
            // Orden que CoinGate ya no tiene (ej. prueba del panel): se reconoce para que no reintente.
            log.info("Aviso de CoinGate de una orden inexistente ({})", id);
            return ResponseEntity.ok().build();
        }
    }

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }
}

package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.entity.OrdenCriptoSimulada;
import com.aluracursos.buensaborbp.infra.security.PedidoSecurity;
import com.aluracursos.buensaborbp.service.PasarelaCriptoSimulada;
import com.aluracursos.buensaborbp.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Set;

// Página de pago de la pasarela cripto simulada. Solo existe con cripto.proveedor=simulado.
// La usa el dueño del pedido (o un admin): ve la orden y simula que pagó o que la orden venció.
// Después de simular, se avisa al pedido igual que lo haría el webhook de una pasarela real.
@RestController
@RequestMapping("/pagos-cripto-simulado")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cripto.proveedor", havingValue = "simulado", matchIfMissing = true)
public class PagoCriptoSimuladoController {

    private static final Set<String> MONEDAS = Set.of("BTC", "ETH", "USDT", "USDC");

    public record OrdenSimuladaResponse(Long id, Long idPedido, BigDecimal montoUsd, String estado,
                                        String moneda, String successUrl, String cancelUrl) {}

    public record SimulacionRequest(String moneda) {}

    private final PasarelaCriptoSimulada pasarela;
    private final PedidoService pedidoService;
    private final PedidoSecurity pedidoSecurity;

    @GetMapping("/{id}")
    public ResponseEntity<OrdenSimuladaResponse> obtener(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(aRespuesta(propia(id, authentication)));
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<OrdenSimuladaResponse> pagar(@PathVariable Long id, @RequestBody(required = false) SimulacionRequest request,
                                                       Authentication authentication) {
        propia(id, authentication);
        String moneda = request != null ? request.moneda() : null;
        if (moneda != null && !MONEDAS.contains(moneda)) {
            throw new IllegalArgumentException("Moneda no soportada: " + moneda);
        }
        return ResponseEntity.ok(aRespuesta(avisar(pasarela.simular(id, "paid", moneda))));
    }

    @PostMapping("/{id}/vencer")
    public ResponseEntity<OrdenSimuladaResponse> vencer(@PathVariable Long id, Authentication authentication) {
        propia(id, authentication);
        return ResponseEntity.ok(aRespuesta(avisar(pasarela.simular(id, "expired", null))));
    }

    // Igual que el aviso de CoinGate: el pedido valida el token y vuelve a consultar la orden.
    private OrdenCriptoSimulada avisar(OrdenCriptoSimulada orden) {
        pedidoService.procesarAvisoCripto(orden.getId(), orden.getIdPedido().toString(), orden.getToken());
        return orden;
    }

    private OrdenCriptoSimulada propia(Long id, Authentication authentication) {
        OrdenCriptoSimulada orden = pasarela.buscar(id);
        boolean esAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!esAdmin && !pedidoSecurity.esDelCliente(authentication, orden.getIdPedido())) {
            throw new AccessDeniedException("Esta orden de pago no es tuya");
        }
        return orden;
    }

    private OrdenSimuladaResponse aRespuesta(OrdenCriptoSimulada o) {
        return new OrdenSimuladaResponse(o.getId(), o.getIdPedido(), o.getMontoUsd(), o.getEstado(), o.getMoneda(),
                o.getSuccessUrl(), o.getCancelUrl());
    }
}

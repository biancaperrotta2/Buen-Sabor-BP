package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.VerificacionPagoResponse;
import com.aluracursos.buensaborbp.dto.CambiarEstadoPedidoRequest;
import com.aluracursos.buensaborbp.dto.PedidoBaseResponse;
import com.aluracursos.buensaborbp.dto.PedidoCajaResponse;
import com.aluracursos.buensaborbp.dto.PedidoCocinaResponse;
import com.aluracursos.buensaborbp.dto.PedidoEntregaResponse;
import com.aluracursos.buensaborbp.dto.PedidoFullResponse;
import com.aluracursos.buensaborbp.dto.PedidoRequest;
import com.aluracursos.buensaborbp.dto.PreferenciaPagoResponse;
import com.aluracursos.buensaborbp.entity.Usuario;
import com.aluracursos.buensaborbp.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    // El cliente se identifica por el JWT, no por un id en el body: evita que alguien arme
    // un pedido "a nombre" de otro cliente.
    @PostMapping
    public ResponseEntity<PedidoFullResponse> crear(@Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(request));
    }

    @GetMapping("/mios")
    public ResponseEntity<List<PedidoBaseResponse>> misPedidos(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.historialCliente(usuario.getId_usuario()));
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    public ResponseEntity<List<PedidoBaseResponse>> historialDeCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(pedidoService.historialCliente(clienteId));
    }

    @GetMapping("/cocina")
    @PreAuthorize("hasAnyRole('ADMIN','COCINERO')")
    public ResponseEntity<List<PedidoCocinaResponse>> paraCocina() {
        return ResponseEntity.ok(pedidoService.listarParaCocina());
    }

    // --- Caja ---

    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    public ResponseEntity<List<PedidoCajaResponse>> pendientes() {
        return ResponseEntity.ok(pedidoService.listarPendientes());
    }

    @GetMapping("/para-entregar")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    public ResponseEntity<List<PedidoCajaResponse>> paraEntregarEnLocal() {
        return ResponseEntity.ok(pedidoService.listarParaEntregarEnLocal());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO','COCINERO','REPARTIDOR') or @pedidoSecurity.esDelCliente(authentication, #id)")
    public ResponseEntity<PedidoFullResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPedidoPorId(id));
    }

    // REPARTIDOR no participa de este endpoint genérico: su flujo pasa por /tomar y
    // /entregar más abajo, que son los únicos que pueden llevar un pedido a EN_CAMINO/ENTREGADO
    // cuando es DELIVERY.
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO','COCINERO')")
    public ResponseEntity<PedidoFullResponse> cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoPedidoRequest request) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, request.nuevoEstado()));
    }

    // Inicia el checkout: crea la preferencia en Mercado Pago y devuelve la URL a la que
    // hay que redirigir al cliente. Solo el dueño del pedido (o un admin) puede iniciarlo.
    @PostMapping("/{id}/pago/preferencia")
    @PreAuthorize("hasRole('ADMIN') or @pedidoSecurity.esDelCliente(authentication, #id)")
    public ResponseEntity<PreferenciaPagoResponse> crearPreferenciaPago(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.crearPreferenciaPago(id));
    }

    // Le pregunta a Mercado Pago cómo salió el pago del pedido y lo actualiza (no depende de que
    // llegue el webhook). Lo usan el cliente al volver del checkout y el cajero desde Caja.
    @PostMapping("/{id}/pago/verificar")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO') or @pedidoSecurity.esDelCliente(authentication, #id)")
    public ResponseEntity<VerificacionPagoResponse> verificarPago(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.verificarPago(id));
    }

    // --- Flujo de repartidor ---

    // Pool de pedidos listos (PREPARADO + DELIVERY) que ningún repartidor tomó todavía.
    @GetMapping("/disponibles-envio")
    @PreAuthorize("hasAnyRole('ADMIN','REPARTIDOR')")
    public ResponseEntity<List<PedidoEntregaResponse>> disponiblesParaEnvio() {
        return ResponseEntity.ok(pedidoService.listarDisponiblesParaEnvio());
    }

    @GetMapping("/mis-entregas")
    @PreAuthorize("hasRole('REPARTIDOR')")
    public ResponseEntity<List<PedidoEntregaResponse>> misEntregas(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.misEntregas(usuario.getId_usuario()));
    }

    // El repartidor autenticado toma un pedido del pool: se autoasigna y el pedido pasa a EN_CAMINO.
    @PatchMapping("/{id}/tomar")
    @PreAuthorize("hasRole('REPARTIDOR')")
    public ResponseEntity<PedidoFullResponse> tomarPedido(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.tomarPedido(id, usuario.getId_usuario()));
    }

    // Cierra la entrega. Solo el repartidor que lo tomó (o un admin) puede marcarlo.
    @PatchMapping("/{id}/entregar")
    @PreAuthorize("hasRole('ADMIN') or @pedidoSecurity.esDelRepartidor(authentication, #id)")
    public ResponseEntity<PedidoFullResponse> marcarEntregado(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.marcarEntregado(id));
    }

    // Paso explícito de "cobré el efectivo", separado de marcar la entrega: lo confirma el
    // cajero (retiro en local) o el repartidor dueño del envío (delivery, cobra en la puerta).
    @PatchMapping("/{id}/pago/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO') or @pedidoSecurity.esDelRepartidor(authentication, #id)")
    public ResponseEntity<PedidoFullResponse> confirmarCobroEfectivo(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.confirmarCobroEfectivo(id));
    }
}

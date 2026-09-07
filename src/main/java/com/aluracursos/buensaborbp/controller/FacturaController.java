package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.FacturaBaseResponse;
import com.aluracursos.buensaborbp.dto.FacturaFullResponse;
import com.aluracursos.buensaborbp.service.FacturaDocumentRenderer;
import com.aluracursos.buensaborbp.service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;
    private final FacturaDocumentRenderer facturaDocumentRenderer;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    public ResponseEntity<List<FacturaBaseResponse>> listar() {
        return ResponseEntity.ok(facturaService.mostrarTodasLasFacturas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    public ResponseEntity<FacturaFullResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(facturaService.buscarDetalleFactura(id));
    }

    // El propio cliente puede ver la factura de un pedido suyo (p. ej. para descargarla).
    @GetMapping("/pedido/{pedidoId}")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO') or @pedidoSecurity.esDelCliente(authentication, #pedidoId)")
    public ResponseEntity<FacturaFullResponse> buscarPorPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(facturaService.buscarPorPedidoId(pedidoId));
    }

    // Generación manual (fuera del flujo automático al marcar un pedido ENTREGADO+PAGADO),
    // para casos de caja donde se necesite emitir la factura antes.
    @PostMapping("/pedido/{pedidoId}")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    public ResponseEntity<FacturaFullResponse> generarDesdePedido(@PathVariable Long pedidoId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaService.generarFacturaDesdePedido(pedidoId));
    }

    // Previsualización en HTML (Diálogo Factura / Diálogo Órdenes | Pestaña Factura).
    @GetMapping(value = "/{id}/preview", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    public ResponseEntity<String> previsualizar(@PathVariable Long id) {
        FacturaFullResponse factura = facturaService.buscarDetalleFactura(id);
        return ResponseEntity.ok(facturaDocumentRenderer.renderizarHtml(factura));
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long id) {
        FacturaFullResponse factura = facturaService.buscarDetalleFactura(id);
        return pdfComoRespuesta(factura);
    }

    // Para el botón "Ver factura" del cliente en Mis Órdenes: abre el PDF en una pestaña nueva.
    @GetMapping(value = "/pedido/{pedidoId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO') or @pedidoSecurity.esDelCliente(authentication, #pedidoId)")
    public ResponseEntity<byte[]> descargarPdfPorPedido(@PathVariable Long pedidoId) {
        FacturaFullResponse factura = facturaService.buscarPorPedidoId(pedidoId);
        return pdfComoRespuesta(factura);
    }

    private ResponseEntity<byte[]> pdfComoRespuesta(FacturaFullResponse factura) {
        byte[] pdf = facturaDocumentRenderer.renderizarPdf(factura);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=factura-" + factura.numeroFactura() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

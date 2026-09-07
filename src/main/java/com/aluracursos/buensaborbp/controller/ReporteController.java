package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.Agrupacion;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoBaseResponse;
import com.aluracursos.buensaborbp.dto.IngresoPeriodoResponse;
import com.aluracursos.buensaborbp.dto.OrdenesPorClienteResponse;
import com.aluracursos.buensaborbp.dto.RankingProductoResponse;
import com.aluracursos.buensaborbp.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// Área de administración: reportes de venta y stock. Todo de solo lectura, JSON puro
// (el front arma las tablas/gráficos/Excel con estos datos).
@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/ranking-productos")
    public ResponseEntity<List<RankingProductoResponse>> rankingProductos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(reporteService.rankingProductos(desde, hasta));
    }

    @GetMapping("/ingresos")
    public ResponseEntity<List<IngresoPeriodoResponse>> ingresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "DIARIO") Agrupacion agrupacion) {
        return ResponseEntity.ok(reporteService.ingresosPorPeriodo(desde, hasta, agrupacion));
    }

    @GetMapping("/ordenes-por-cliente")
    public ResponseEntity<List<OrdenesPorClienteResponse>> ordenesPorCliente(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(reporteService.ordenesPorCliente(desde, hasta));
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<ArticuloInsumoBaseResponse>> stockBajo() {
        return ResponseEntity.ok(reporteService.stockBajoMinimo());
    }
}

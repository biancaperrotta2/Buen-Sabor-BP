package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.DomicilioBaseResponse;
import com.aluracursos.buensaborbp.dto.DomicilioFullResponse;
import com.aluracursos.buensaborbp.dto.DomicilioRequest;
import com.aluracursos.buensaborbp.service.DomicilioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Gestión administrativa de domicilios ya existentes en el sistema (de cualquier cliente/sucursal).
// El alta de "mi domicilio" para un cliente es autoservicio: ver POST /clientes/me/domicilios.
@RestController
@RequestMapping("/domicilios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DomicilioController {

    private final DomicilioService domicilioService;

    @GetMapping
    public ResponseEntity<List<DomicilioBaseResponse>> listar() {
        return ResponseEntity.ok(domicilioService.mostrarDomicilios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DomicilioBaseResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(domicilioService.buscarDomicilioID(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DomicilioFullResponse> actualizar(@PathVariable Long id, @Valid @RequestBody DomicilioRequest request) {
        return ResponseEntity.ok(domicilioService.actualizarDomicilio(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        domicilioService.desactivarDomicilio(id);
        return ResponseEntity.noContent().build();
    }
}

package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.PageResponse;
import com.aluracursos.buensaborbp.dto.PromocionBaseResponse;
import com.aluracursos.buensaborbp.dto.PromocionFullResponse;
import com.aluracursos.buensaborbp.dto.PromocionRequest;
import com.aluracursos.buensaborbp.service.PromocionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/promociones")
@RequiredArgsConstructor
public class PromocionController {

    private final PromocionService promocionService;

    // Catálogo público: promociones vigentes (fecha/hora + activa) para el menú del cliente.
    @GetMapping
    public ResponseEntity<List<PromocionBaseResponse>> listarVigentes() {
        return ResponseEntity.ok(promocionService.listarParaMenu());
    }

    // Para la tabla de admin: búsqueda + paginado + orden, activas e inactivas por igual.
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<PromocionBaseResponse>> listarAdmin(
            @RequestParam(required = false, defaultValue = "") String busqueda,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(promocionService.listarAdmin(busqueda, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromocionFullResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(promocionService.buscarPorIdFull(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromocionFullResponse> crear(@Valid @RequestBody PromocionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promocionService.crearPromocion(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromocionFullResponse> actualizar(@PathVariable Long id, @Valid @RequestBody PromocionRequest request) {
        return ResponseEntity.ok(promocionService.actualizarPromocion(id, request));
    }

    // Baja lógica: alterna activo/inactivo (no hay borrado físico, mismo patrón que el resto del admin).
    @PatchMapping("/{id}/habilitar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cambiarEstadoHabilitacion(@PathVariable Long id) {
        promocionService.cambiarEstadoHabilitacion(id);
        return ResponseEntity.noContent().build();
    }
}

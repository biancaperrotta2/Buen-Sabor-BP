package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoRequest;
import com.aluracursos.buensaborbp.service.ArticuloManufacturadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articulo-manufacturado")
@RequiredArgsConstructor
public class ArticuloManufacturadoController {

    private final ArticuloManufacturadoService articuloManufacturadoService;

    @GetMapping
    public ResponseEntity<List<ArticuloManufacturadoBaseResponse>> listar() {
        return ResponseEntity.ok(articuloManufacturadoService.getAll().getContent());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ArticuloManufacturadoBaseResponse>> listarActivos() {
        return ResponseEntity.ok(articuloManufacturadoService.getActivos().getContent());
    }

    // Gestor de semielaborados: son insumos internos de otras recetas, no catálogo público.
    @GetMapping("/semielaborados")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ArticuloManufacturadoBaseResponse>> listarSemielaborados() {
        return ResponseEntity.ok(articuloManufacturadoService.getSemielaborados().getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticuloManufacturadoFullResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(articuloManufacturadoService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArticuloManufacturadoFullResponse> crear(@Valid @RequestBody ArticuloManufacturadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(articuloManufacturadoService.crearManufacturado(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArticuloManufacturadoFullResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ArticuloManufacturadoRequest request) {
        return ResponseEntity.ok(articuloManufacturadoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        articuloManufacturadoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

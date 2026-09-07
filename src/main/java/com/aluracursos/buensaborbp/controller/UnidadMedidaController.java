package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.UnidadMedidaRequest;
import com.aluracursos.buensaborbp.dto.UnidadMedidaResponse;
import com.aluracursos.buensaborbp.entity.UnidadMedida;
import com.aluracursos.buensaborbp.mapper.UnidadMedidaMapper;
import com.aluracursos.buensaborbp.service.UnidadMedidaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unidad-medida")
@RequiredArgsConstructor
public class UnidadMedidaController {

    private final UnidadMedidaService unidadMedidaService;
    private final UnidadMedidaMapper unidadMedidaMapper;

    @GetMapping("/all")
    public ResponseEntity<List<UnidadMedidaResponse>> listar() {
        return ResponseEntity.ok(unidadMedidaMapper.toResponseList(unidadMedidaService.findAll()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnidadMedidaResponse> crear(@Valid @RequestBody UnidadMedidaRequest request) {
        UnidadMedida creada = unidadMedidaService.create(unidadMedidaMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(unidadMedidaMapper.toResponse(creada));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnidadMedidaResponse> actualizar(@PathVariable Long id, @Valid @RequestBody UnidadMedidaRequest request) {
        UnidadMedida entidad = unidadMedidaMapper.toEntity(request);
        entidad.setId(id);
        return ResponseEntity.ok(unidadMedidaMapper.toResponse(unidadMedidaService.update(id, entidad)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        unidadMedidaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

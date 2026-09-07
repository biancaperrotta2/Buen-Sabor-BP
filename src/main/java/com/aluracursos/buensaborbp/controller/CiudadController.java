package com.aluracursos.buensaborbp.controller;

import org.springframework.web.bind.annotation.RestController;

import com.aluracursos.buensaborbp.dto.ActualizarCostoEnvioRequest;
import com.aluracursos.buensaborbp.dto.CiudadRequest;
import com.aluracursos.buensaborbp.dto.CiudadResponse;
import com.aluracursos.buensaborbp.service.CiudadService;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/ciudades")
@RequiredArgsConstructor
public class CiudadController {
    private final CiudadService ciudadService;

    // GET - Lista todas las ciudades
    @GetMapping("/all")
    public ResponseEntity<List<CiudadResponse>> listarCiudades() {
        List<CiudadResponse> ciudades = ciudadService.getAll();
        return ResponseEntity.ok(ciudades);
    }

    @GetMapping("/provincia/{provinciaId}")
    public ResponseEntity<List<CiudadResponse>> listarPorProvincia(@PathVariable Long provinciaId) {
        return ResponseEntity.ok(ciudadService.listarPorProvincia(provinciaId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CiudadResponse> crear(@Valid @RequestBody CiudadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ciudadService.crearCiudad(request));
    }

    @PutMapping("/{id}/costo-envio")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CiudadResponse> actualizarCostoEnvio(@PathVariable Long id,
                                                                @Valid @RequestBody ActualizarCostoEnvioRequest request) {
        return ResponseEntity.ok(ciudadService.actualizarCostoEnvio(id, request.costoEnvio()));
    }
}

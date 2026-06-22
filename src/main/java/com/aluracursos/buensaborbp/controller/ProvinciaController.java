package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.ProvinciaRequest;
import com.aluracursos.buensaborbp.dto.ProvinciaResponse;
import com.aluracursos.buensaborbp.service.ProvinciaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/provincias")
@CrossOrigin(origins = "*")
public class ProvinciaController {

    private final ProvinciaService provinciaService;

    public ProvinciaController(ProvinciaService provinciaService) {
        this.provinciaService = provinciaService;
    }

    // CUALQUIERA (Cliente o Admin) puede ver las provincias para registrarse
    @GetMapping
    public ResponseEntity<List<ProvinciaResponse>> getAll() {
        return ResponseEntity.ok(provinciaService.listarTodas());
    }

    // SOLO EL ADMIN puede agregar nuevas provincias
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<ProvinciaResponse> create(@RequestBody ProvinciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(provinciaService.crearProvincia(request));
    }
}
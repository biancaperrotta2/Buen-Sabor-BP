package com.aluracursos.buensaborbp.controller;

import org.springframework.web.bind.annotation.RestController;

import com.aluracursos.buensaborbp.dto.CiudadResponse;
import com.aluracursos.buensaborbp.service.CiudadService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;

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

    // GET - Lista solo las ciudades activas
    @GetMapping("/activos")
    public ResponseEntity<List<CiudadResponse>> listarCiudadesActivas() {
        List<CiudadResponse> ciudades = ciudadService.getActivos();
        return ResponseEntity.ok(ciudades);
    }
}
    


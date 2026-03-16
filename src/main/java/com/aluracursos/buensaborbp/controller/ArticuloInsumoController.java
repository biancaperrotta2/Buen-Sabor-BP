package com.aluracursos.buensaborbp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aluracursos.buensaborbp.dto.ArticuloInsumoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoRequest;
import com.aluracursos.buensaborbp.service.ArticuloInsumoServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/insumos")
@RequiredArgsConstructor
public class ArticuloInsumoController {

    private final ArticuloInsumoServiceImpl servicio;

    //GET - Lista todos los insumos
    @GetMapping("/all")
    public ResponseEntity<List<ArticuloInsumoBaseResponse>> listarInsumos() {
        List<ArticuloInsumoBaseResponse> insumos = servicio.getAll();
        return ResponseEntity.ok(insumos);
    }

    //GET - Lista solo los insumos activos
    @GetMapping("/activos")
    public ResponseEntity<List<ArticuloInsumoBaseResponse>> listarActivos() {
        List<ArticuloInsumoBaseResponse> activos = servicio.getActivos();
        return ResponseEntity.ok(activos);
    }

    //GET - Devuelvr un insumo por ID
    @GetMapping("/{id}")
    public ResponseEntity<ArticuloInsumoFullResponse> obtenerPorId(@PathVariable Long id) {
        ArticuloInsumoFullResponse response = servicio.getById(id);
        return ResponseEntity.ok(response);
    }

    //POST - Crea un nuevo insumo
    @PostMapping
    public ResponseEntity<ArticuloInsumoFullResponse> crearInsumo(@RequestBody ArticuloInsumoRequest request) {
        ArticuloInsumoFullResponse creado = servicio.crearArticuloInsumo(request);
        return ResponseEntity.ok(creado);
    }

    //PUT - Actualiza un insumo existente
    @PutMapping("/{id}")
    public ResponseEntity<ArticuloInsumoFullResponse> actualizarInsumo(
            @PathVariable Long id,
            @RequestBody ArticuloInsumoRequest request) {

        ArticuloInsumoFullResponse actualizado = servicio.update(id, request);
        return ResponseEntity.ok(actualizado);
    }

    //DELETE - Elimina un insumo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarInsumo(@PathVariable Long id) {
        servicio.delete(id);
        return ResponseEntity.noContent().build();
    }
}

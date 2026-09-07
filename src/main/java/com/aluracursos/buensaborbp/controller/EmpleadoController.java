package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.EmpleadoBaseResponse;
import com.aluracursos.buensaborbp.dto.EmpleadoFullResponse;
import com.aluracursos.buensaborbp.dto.EmpleadoRequest;
import com.aluracursos.buensaborbp.service.EmpleadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Alta de empleados: la hace un ADMIN, no es autorregistro (a diferencia de /clientes/registro)
@RestController
@RequestMapping("/empleados")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    @PostMapping
    public ResponseEntity<EmpleadoFullResponse> crear(@Valid @RequestBody EmpleadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empleadoService.crearEmpleado(request));
    }

    @GetMapping
    public ResponseEntity<List<EmpleadoBaseResponse>> listarEmpleados() {
        return ResponseEntity.ok(empleadoService.mostrarEmpleados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoBaseResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.buscarEmpleadoID(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpleadoFullResponse> actualizar(@PathVariable Long id, @Valid @RequestBody EmpleadoRequest request) {
        return ResponseEntity.ok(empleadoService.actualizarEmpleado(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        empleadoService.desactivarEmpleado(id);
        return ResponseEntity.noContent().build();
    }
}

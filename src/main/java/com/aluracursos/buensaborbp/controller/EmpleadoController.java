package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.EmpleadoBaseResponse;
import com.aluracursos.buensaborbp.dto.EmpleadoFullResponse;
import com.aluracursos.buensaborbp.dto.EmpleadoRequest;
import com.aluracursos.buensaborbp.dto.PageResponse;
import com.aluracursos.buensaborbp.service.EmpleadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.aluracursos.buensaborbp.entity.Usuario;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<PageResponse<EmpleadoBaseResponse>> listarEmpleados(
            @RequestParam(required = false, defaultValue = "") String busqueda,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(empleadoService.mostrarEmpleados(busqueda, pageable));
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
    public ResponseEntity<Void> desactivar(@PathVariable Long id, @AuthenticationPrincipal Usuario actual) {
        // Si el único admin se desactivara, nadie podría volver a activar cuentas desde la app.
        if (actual != null && id.equals(actual.getId_usuario())) {
            throw new IllegalArgumentException("No podés desactivar tu propia cuenta");
        }
        empleadoService.desactivarEmpleado(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<Void> reactivar(@PathVariable Long id) {
        empleadoService.reactivarEmpleado(id);
        return ResponseEntity.noContent().build();
    }
}

package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.ActualizarCantidadCarritoRequest;
import com.aluracursos.buensaborbp.dto.AgregarItemCarritoRequest;
import com.aluracursos.buensaborbp.dto.CarritoResponse;
import com.aluracursos.buensaborbp.entity.Usuario;
import com.aluracursos.buensaborbp.service.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Autoservicio: siempre opera sobre el carrito del cliente autenticado (nunca recibe un
// id de cliente en la URL), mismo criterio que ClienteController#/me/domicilios.
@RestController
@RequestMapping("/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    public ResponseEntity<CarritoResponse> miCarrito(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(carritoService.obtenerMiCarrito(usuario.getId_usuario()));
    }

    @PostMapping("/items")
    public ResponseEntity<CarritoResponse> agregarItem(@AuthenticationPrincipal Usuario usuario,
                                                         @Valid @RequestBody AgregarItemCarritoRequest request) {
        return ResponseEntity.ok(carritoService.agregarItem(usuario.getId_usuario(), request));
    }

    @PutMapping("/items/{idArticulo}")
    public ResponseEntity<CarritoResponse> actualizarCantidad(@AuthenticationPrincipal Usuario usuario,
                                                                @PathVariable Long idArticulo,
                                                                @Valid @RequestBody ActualizarCantidadCarritoRequest request) {
        return ResponseEntity.ok(carritoService.actualizarCantidad(usuario.getId_usuario(), idArticulo, request.cantidad()));
    }

    @DeleteMapping("/items/{idArticulo}")
    public ResponseEntity<CarritoResponse> eliminarItem(@AuthenticationPrincipal Usuario usuario,
                                                          @PathVariable Long idArticulo) {
        return ResponseEntity.ok(carritoService.eliminarItem(usuario.getId_usuario(), idArticulo));
    }

    // "Cancelar pedido": vacía el carrito sin tocar ningún pedido ya creado.
    @DeleteMapping
    public ResponseEntity<Void> vaciar(@AuthenticationPrincipal Usuario usuario) {
        carritoService.vaciarCarrito(usuario.getId_usuario());
        return ResponseEntity.noContent().build();
    }
}

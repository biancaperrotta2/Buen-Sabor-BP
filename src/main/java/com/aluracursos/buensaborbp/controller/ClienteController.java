package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.ClienteBaseResponse;
import com.aluracursos.buensaborbp.dto.ClienteFullResponse;
import com.aluracursos.buensaborbp.dto.ClienteRequest;
import com.aluracursos.buensaborbp.dto.DomicilioBaseResponse;
import com.aluracursos.buensaborbp.dto.DomicilioFullResponse;
import com.aluracursos.buensaborbp.dto.DomicilioRequest;
import com.aluracursos.buensaborbp.entity.Usuario;
import com.aluracursos.buensaborbp.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    // POST público: autorregistro de clientes, sin token (igual que /login)
    @PostMapping("/registro")
    public ResponseEntity<ClienteFullResponse> registrarse(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crearCliente(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClienteBaseResponse>> listarClientes() {
        return ResponseEntity.ok(clienteService.mostrarClientes());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @clienteSecurity.esPropio(authentication, #id)")
    public ResponseEntity<ClienteBaseResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarClienteID(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @clienteSecurity.esPropio(authentication, #id)")
    public ResponseEntity<ClienteFullResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        clienteService.desactivarCliente(id);
        return ResponseEntity.noContent().build();
    }

    // Autoservicio: el cliente logueado agrega un domicilio propio para poder usarlo en un pedido.
    @PostMapping("/me/domicilios")
    public ResponseEntity<DomicilioFullResponse> agregarMiDomicilio(@AuthenticationPrincipal Usuario usuario,
                                                                     @Valid @RequestBody DomicilioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clienteService.agregarDomicilio(usuario.getId_usuario(), request));
    }

    @GetMapping("/me/domicilios")
    public ResponseEntity<List<DomicilioBaseResponse>> misDomicilios(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(clienteService.misDomicilios(usuario.getId_usuario()));
    }
}

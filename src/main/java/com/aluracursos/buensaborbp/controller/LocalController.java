package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.LocalResponse;
import com.aluracursos.buensaborbp.repository.SucursalRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;

// Público (ver SecurityConfig): el local es uno solo (la primera sucursal activa, igual que al
// crear un pedido). Lo usa el carrito para "¿Cómo llegar?" y el aviso de local cerrado.
@RestController
@RequestMapping("/local")
@RequiredArgsConstructor
public class LocalController {

    private final SucursalRepository sucursalRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<LocalResponse> obtener() {
        return sucursalRepository.findByActivoTrue().stream().findFirst()
                .map(s -> new LocalResponse(s.getNombre(), s.direccionTexto(), s.getHorarioApertura(),
                        s.getHorarioCierre(), s.estaAbierta(LocalTime.now())))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("No hay ningún local cargado"));
    }
}

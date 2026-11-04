package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.SesionResponse;
import com.aluracursos.buensaborbp.entity.Usuario;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sesion")
public class SesionController {

    // El rol sale de la base (SecurityFilter recarga el Usuario), no del payload del token,
    // así el frontend puede confirmar contra el servidor antes de mostrar pantallas de rol.
    @GetMapping
    public SesionResponse actual(@AuthenticationPrincipal Usuario usuario) {
        return new SesionResponse(usuario.getUsuario(), usuario.getRol().name());
    }
}

package com.aluracursos.buensaborbp.infra.security;

import com.aluracursos.buensaborbp.entity.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("clienteSecurity")
public class ClienteSecurity {

    public boolean esPropio(Authentication authentication, Long id) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            return false;
        }
        return usuario.getId_usuario().equals(id);
    }
}

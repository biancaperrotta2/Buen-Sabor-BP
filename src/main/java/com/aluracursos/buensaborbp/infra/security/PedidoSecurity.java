package com.aluracursos.buensaborbp.infra.security;

import com.aluracursos.buensaborbp.entity.Usuario;
import com.aluracursos.buensaborbp.repository.PedidoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("pedidoSecurity")
public class PedidoSecurity {

    private final PedidoRepository pedidoRepository;

    public PedidoSecurity(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public boolean esDelCliente(Authentication authentication, Long pedidoId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            return false;
        }
        return pedidoRepository.findById(pedidoId)
                .map(pedido -> pedido.getCliente() != null
                        && pedido.getCliente().getId_usuario().equals(usuario.getId_usuario()))
                .orElse(false);
    }

    public boolean esDelRepartidor(Authentication authentication, Long pedidoId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            return false;
        }
        return pedidoRepository.findById(pedidoId)
                .map(pedido -> pedido.getRepartidor() != null
                        && pedido.getRepartidor().getId_usuario().equals(usuario.getId_usuario()))
                .orElse(false);
    }
}

package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.dto.ImagenUsuarioResponse;
import java.util.List;

public record ClienteBaseResponse(
    Long id,
    String nombre,
    String apellido,
    ImagenUsuarioResponse fotoPerfilCliente,
    String email,
    String telefono,
    List<PedidoBaseResponse> pedidos
) {}

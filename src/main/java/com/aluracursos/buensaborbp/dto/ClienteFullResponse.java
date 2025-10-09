package com.aluracursos.buensaborbp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteFullResponse {
    private Long id;
    private String nombreApellido;
    private String email;
    private String telefono;
    private ImagenUsuarioResponse fotoPerfilCliente;
    private List<PedidoBaseResponse> pedidos;
}

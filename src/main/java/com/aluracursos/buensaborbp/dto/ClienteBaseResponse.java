package com.aluracursos.buensaborbp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteBaseResponse {
    private Long id;
    private String nombreApellido;
    private ImagenUsuarioResponse fotoPerfilCliente;
    private String email;
    private String telefono;
    private DomicilioBaseResponse domicilio;
}

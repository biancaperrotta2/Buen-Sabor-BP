package com.aluracursos.buensaborbp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmpleadoFullResponse {
    private String nombreApellido;
    private String email;
    private String telefono;
    private ImagenUsuarioResponse imagenEmpleado;
}

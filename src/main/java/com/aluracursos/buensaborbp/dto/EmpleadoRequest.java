package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmpleadoRequest {
    private String nombreApellido;
    private String email;
    private String telefono;
    private Rol cargo;
    private ImagenUsuarioRequest imagenEmpleado;
}

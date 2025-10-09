package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Ciudad;
import com.aluracursos.buensaborbp.entity.Localidad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DomicilioFullResponse {
    private String calle;
    private String numero;
    private String piso;
    private String departamento;
    private Ciudad ciudad;
    private Localidad localidad;
    private String aclaraciones;
    private String alias;
}

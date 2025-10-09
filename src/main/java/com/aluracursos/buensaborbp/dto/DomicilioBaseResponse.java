package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Ciudad;
import com.aluracursos.buensaborbp.entity.Localidad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DomicilioBaseResponse {
    private String alias;
    private String calle;
    private String numero;
    private Ciudad ciudad;
    private Localidad localidad;
}

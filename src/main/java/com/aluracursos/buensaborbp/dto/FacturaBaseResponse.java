package com.aluracursos.buensaborbp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacturaBaseResponse {
    private Long idFactura;
    private Long idCliente;
    private LocalDate fecha;
    private BigDecimal total;
    private String numeroFactura;
}

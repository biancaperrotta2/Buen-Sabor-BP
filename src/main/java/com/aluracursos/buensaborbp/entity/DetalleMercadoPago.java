package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "datos_mercadopago")
public class DetalleMercadoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // El id de pago que devuelve Mercado Pago es Long (puede superar el rango de Integer).
    private Long id_Pago;
    private Integer id_Orden;
    private String idPreferencia;
    private String tipoPago;
}

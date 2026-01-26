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
    private Integer id_Pago;
    private Integer id_Orden;
    private String idPreferencia;
    private String tipoPago;
}

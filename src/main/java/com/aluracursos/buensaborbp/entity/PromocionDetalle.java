package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
@Entity(name = "promocion_detalle")
public class PromocionDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_promocion;

    private Integer cantidad;

    @ManyToOne
    @JoinColumn(name = "articulo_id")
    private Articulo articulos;

    @ManyToOne
    @JoinColumn(name = "promocion_id")
    private Promocion articuloPromocion;
}

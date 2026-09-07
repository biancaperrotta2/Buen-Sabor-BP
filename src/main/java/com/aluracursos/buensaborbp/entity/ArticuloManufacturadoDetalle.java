package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticuloManufacturadoDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_articuloManufacturadoDetalle;

    @Column(nullable = false)
    private Double cantidad;

    // Puede ser un ArticuloInsumo o un ArticuloManufacturado marcado esSemielaborado=true;
    // nunca un plato final ni una Promocion (se valida al armar el detalle, ver
    // ArticuloManufacturadoServiceImpl.construirDetalles).
    @ManyToOne()
    @JoinColumn(name = "componente_id")
    private Articulo componente;

    @ManyToOne
    @JoinColumn(name = "articulo_manufacturado_id", nullable = false)
    private ArticuloManufacturado articuloManufacturado;
}

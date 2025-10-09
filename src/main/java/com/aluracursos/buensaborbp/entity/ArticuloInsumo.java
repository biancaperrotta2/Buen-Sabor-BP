package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ArticuloInsumo extends Articulo{
    @Column(nullable = false)
    private BigDecimal precioCompra;

    @Column(nullable = false)
    private Integer stockActual;

    @Column(nullable = false)
    private Integer stockMaximo;

    @Column(nullable = false)
    private Integer stockMinimo;

    @Column(nullable = false)
    private boolean esInsumo;
}

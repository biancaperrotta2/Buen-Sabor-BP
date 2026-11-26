package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDate;

// Una compra de un insumo (historias #27 y #28, figuras 66 y 67). Es un registro simple, no un
// lote: al cargarla suma al stock actual del insumo, al corregirla ajusta la diferencia y al
// borrarla la descuenta. El vencimiento es informativo (las ventas no consumen por compra).
// La cantidad se guarda en la unidad base del insumo (kg / l / unidad), como su stock, y el
// costo unitario es por esa unidad base (ej. $ por kg).
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExistenciaInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "insumo_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private ArticuloInsumo insumo;

    @Column(nullable = false)
    private LocalDate fechaCompra;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Column(nullable = false)
    private Double cantidad;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal costoUnitario;
}

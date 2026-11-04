package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// Un lote de un semielaborado preparado en cocina (ej. 5 kg de masa hechos el lunes). El stock
// del semielaborado es la suma de lo que queda en sus lotes vigentes; los pedidos consumen
// primero el lote que vence antes. Cantidades en la unidad del semielaborado (siempre una
// unidad base: Kilos, Litros o Unidad).
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExistenciaSemielaborado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semielaborado_id")
    @ToString.Exclude
    private ArticuloManufacturado semielaborado;

    @Column(nullable = false)
    private LocalDate fechaElaboracion;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    // Lo que se preparó y lo que todavía queda sin usar en pedidos.
    @Column(nullable = false)
    private Double cantidad;

    @Column(nullable = false)
    private Double cantidadRestante;

    // Costo de una unidad al momento de prepararlo (lo que costaban los insumos ese día).
    @Column(precision = 15, scale = 2)
    private BigDecimal costoUnitario;

    // Un lote vencido que se dio de baja (se tiró): ya no cuenta ni se puede usar.
    @Builder.Default
    @Column(columnDefinition = "bit not null default 0")
    private Boolean descartado = false;

    public boolean estaVencido(LocalDate hoy) {
        return fechaVencimiento.isBefore(hoy);
    }

    // Disponible para pedidos: no descartado, no vencido y con algo restante.
    public boolean estaVigente(LocalDate hoy) {
        return !Boolean.TRUE.equals(descartado) && !estaVencido(hoy) && cantidadRestante > 0;
    }

    // Todavía no se usó nada en pedidos: solo así se puede corregir o borrar devolviendo insumos.
    public boolean sinUsar() {
        return Double.compare(cantidadRestante, cantidad) == 0;
    }
}

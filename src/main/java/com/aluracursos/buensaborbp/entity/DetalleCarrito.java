package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetalleCarrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer cantidad;

    @ManyToOne
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    // No es un campo persistido (la clase usa field-access, @Id está en un field): a
    // diferencia de DetallePedido, que congela el precio al momento de comprar, el carrito
    // debe reflejar siempre el precio vigente del artículo, así que se calcula al vuelo.
    public BigDecimal getSubtotal() {
        if (articulo == null || articulo.getPrecioVenta() == null || cantidad == null) {
            return BigDecimal.ZERO;
        }
        return articulo.getPrecioVenta().multiply(BigDecimal.valueOf(cantidad));
    }
}

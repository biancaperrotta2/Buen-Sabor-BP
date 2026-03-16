package com.aluracursos.buensaborbp.entity;

import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.FormaPago;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime horaEstimadaFinalizacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estadoPedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estadoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEnvio tipoEnvio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPago formaPago;

    @Column(nullable = false)
    private LocalDate fechaPedido;

    private BigDecimal costoTotal;

    private BigDecimal total;

    private BigDecimal gastosEnvio;

    private String aclaracionDomicilio;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detallePedidos = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domicilio_id")
    private Domicilio domicilio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // Guarda el domicilio textual del momento del pedido
    private String domicilioSnapshot;

    // === MÉTODOS ===
    public void calcularTotal() {
        BigDecimal total = BigDecimal.ZERO;

        if (detallePedidos != null && !detallePedidos.isEmpty()) {
            for (DetallePedido detalle : detallePedidos) {
                if (detalle != null && detalle.getSubtotal() != null) {
                    total = total.add(detalle.getSubtotal());
                }
            }
        }

        if (tipoEnvio == TipoEnvio.DELIVERY && gastosEnvio != null) {
            total = total.add(gastosEnvio);
        }

        this.total = total;
    }

    public void calcularCostoTotal() {
        if (detallePedidos == null || detallePedidos.isEmpty()) {
            this.costoTotal = BigDecimal.ZERO;
            return;
        }

        BigDecimal costo = BigDecimal.ZERO;

        for (DetallePedido detalle : detallePedidos) {
            if (detalle == null || detalle.getArticulo() == null) {
                continue;
            }
            
            Articulo articulo = detalle.getArticulo();
            BigDecimal costoUnitario = articulo.getPrecioCosto();

            if (costoUnitario == null) {
                throw new IllegalStateException("El artículo " + articulo.getNombre() + " no tiene precio de costo definido.");
            }

            costo = costo.add(costoUnitario.multiply(BigDecimal.valueOf(detalle.getCantidad())));
        }

        this.costoTotal = costo;
    }

    public void reservarStock() {
        if (detallePedidos == null || detallePedidos.isEmpty()) {
            return;
        }

        for (DetallePedido detalle : detallePedidos) {
            if (detalle == null || detalle.getArticulo() == null) {
                continue;
            }
            
            Articulo articulo = detalle.getArticulo();
            articulo.reservarStock(detalle.getCantidad(), articulo.getNombre());
        }
    }

    public void confirmarStock() {
        if (detallePedidos == null || detallePedidos.isEmpty()) {
            return;
        }

        for (DetallePedido detalle : detallePedidos) {
            if (detalle == null || detalle.getArticulo() == null) {
                continue;
            }
            
            Articulo articulo = detalle.getArticulo();
            articulo.confirmarStock(detalle.getCantidad());
        }
    }

    public void liberarStock() {
        if (detallePedidos == null || detallePedidos.isEmpty()) {
            return;
        }

        for (DetallePedido detalle : detallePedidos) {
            if (detalle == null || detalle.getArticulo() == null) {
                continue;
            }
            
            Articulo articulo = detalle.getArticulo();
            articulo.liberarStock(detalle.getCantidad());
        }
    }
}

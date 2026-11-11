package com.aluracursos.buensaborbp.entity;

import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.EstadoPago;
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
    private Estado estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPago estadoPago;

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

    // Monto (no porcentaje) del 10% que se descuenta cuando el pedido es TAKE_AWAY.
    // Null si es DELIVERY, donde no aplica descuento.
    private BigDecimal descuento;

    private String aclaracionDomicilio;

    // Pedido de cocina del cliente (ej. "sin cebolla"); lo ven cocina y caja. La
    // aclaración del domicilio (timbre, piso) va aparte, para el repartidor.
    @Column(length = 255)
    private String aclaraciones;

    // Lo escribe el cajero al rechazar el pedido; el cliente lo ve en "Mis pedidos".
    @Column(length = 255)
    private String motivoCancelacion;

    // Se rechazó un pedido cobrado por Mercado Pago y MP no aceptó el reintegro automático: el
    // cajero tiene que devolverlo desde el panel de MP. Sigue PAGADO hasta que se devuelva.
    @Builder.Default
    private Boolean reintegroPendiente = false;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detallePedidos = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    // El pago se confirma (webhook de Mercado Pago) mucho antes de que exista la Factura
    // (que recién se genera al marcar el pedido ENTREGADO), por eso el dato de pago vive acá.
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "detalle_mercado_pago_id")
    private DetalleMercadoPago detalleMercadoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_domicilio")
    private Domicilio domicilio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // Se asigna cuando un REPARTIDOR toma el pedido (ver PedidoServiceImpl.tomarPedido);
    // null hasta ese momento y para pedidos que son TAKE_AWAY.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id")
    private Empleado repartidor;

    // Guarda el domicilio textual del momento del pedido
    private String domicilioSnapshot;

    // === MÉTODOS ===

    // Retiro en el local: 10% de descuento sobre el subtotal de productos (política del negocio).
    private static final BigDecimal PORCENTAJE_DESCUENTO_RETIRO_LOCAL = new BigDecimal("0.10");

    public void calcularTotal() {
        BigDecimal subtotalProductos = BigDecimal.ZERO;

        if (detallePedidos != null && !detallePedidos.isEmpty()) {
            for (DetallePedido detalle : detallePedidos) {
                if (detalle != null && detalle.getSubtotal() != null) {
                    subtotalProductos = subtotalProductos.add(detalle.getSubtotal());
                }
            }
        }

        BigDecimal total = subtotalProductos;

        if (tipoEnvio == TipoEnvio.TAKE_AWAY) {
            this.descuento = subtotalProductos.multiply(PORCENTAJE_DESCUENTO_RETIRO_LOCAL);
            total = total.subtract(this.descuento);
        } else {
            this.descuento = null;
        }

        if (tipoEnvio == TipoEnvio.DELIVERY && gastosEnvio != null) {
            total = total.add(gastosEnvio);
        }

        this.total = total;
    }

    // El cocinero demora el pedido: pospone la hora estimada de entrega 10 minutos.
    // Se puede llamar más de una vez si se vuelve a demorar (acumula).
    public void demorar() {
        if (horaEstimadaFinalizacion != null) {
            this.horaEstimadaFinalizacion = horaEstimadaFinalizacion.plusMinutes(10);
        }
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

package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Promocion extends Articulo {

    @Column(nullable = false)
    private LocalDate fechaDesde;

    @Column(nullable = false)
    private LocalDate fechaHasta;

    @Column(nullable = false)
    private LocalTime horaDesde;

    @Column(nullable = false)
    private LocalTime horaHasta;

    @Column(nullable = false)
    private String descripcionDescuento;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioTotal;

    @OneToMany(mappedBy = "articuloPromocion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromocionDetalle> promocionDetalle = new ArrayList<>();

    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImagenPromocion> imagenes = new ArrayList<>();

    // ================== MÉTODOS DE CÁLCULO ================== //
    public void calcularPrecioCosto() {
        BigDecimal costoTotal = BigDecimal.ZERO;

        for (PromocionDetalle detalle : promocionDetalle) {
            Articulo articulo = detalle.getArticulos(); // obtenemos el artículo
            BigDecimal cantidad = BigDecimal.valueOf(detalle.getCantidad()); // cantidad de ese artículo

            if (articulo instanceof ArticuloInsumo insumo) {
                // Si es un insumo, convertimos la cantidad a la unidad base
                BigDecimal cantidadBase = BigDecimal.valueOf(insumo.convertirACantidadBase(cantidad.doubleValue()));
                BigDecimal precioPorCantidad = insumo.getPrecioCosto().multiply(cantidadBase);
                costoTotal = costoTotal.add(precioPorCantidad);
            } else {
                // Si es manufacturado o promoción, multiplicamos costo por cantidad
                BigDecimal precioPorCantidad = articulo.getPrecioCosto().multiply(cantidad);
                costoTotal = costoTotal.add(precioPorCantidad);
            }
        }
        setPrecioCosto(costoTotal);
    }

    public void calcularPrecioTotal() {
        BigDecimal ventaTotal = BigDecimal.ZERO;

        for (PromocionDetalle detalle : this.getPromocionDetalle()) {
            BigDecimal precio = detalle.getArticulos().getPrecioVenta();
            BigDecimal cantidad = BigDecimal.valueOf(detalle.getCantidad());
            BigDecimal precioPorCantidad = precio.multiply(cantidad);
            ventaTotal = ventaTotal.add(precioPorCantidad);
        }
        setPrecioTotal(ventaTotal);
    }

    public void calcularPrecioVenta(BigDecimal precioVentaManual) {
        if (precioVentaManual != null) {
            // Precio definido manualmente por el usuario
            setPrecioVenta(precioVentaManual);
        } else {
            // Si no se define manualmente, usar la suma total de los artículos
            if (getPrecioTotal() == null) {
                calcularPrecioTotal(); // aseguramos tener el valor calculado
            }
            setPrecioVenta(getPrecioTotal());
        }
    }

    // ================== MANEJO DE STOCK ================== //

    public void reservarStock(double cantidad, String articuloPadreNombre) {
        for (PromocionDetalle detalle : this.getPromocionDetalle()) {
            Articulo articulo = detalle.getArticulos();
            double cantTotal = detalle.getCantidad() * cantidad;

            if (articulo instanceof ArticuloManufacturado manufacturado) {
                manufacturado.reservarStock(cantTotal, articuloPadreNombre);
            } else if (articulo instanceof ArticuloInsumo insumo) {
                double requerido = insumo.convertirACantidadBase(cantTotal);
                insumo.reservarStock(requerido, articuloPadreNombre);
            }
        }
    }

    public void liberarStock(double cantidad) {
        for (PromocionDetalle detalle : this.getPromocionDetalle()) {
            Articulo articulo = detalle.getArticulos();
            double cantTotal = detalle.getCantidad() * cantidad;

            if (articulo instanceof ArticuloManufacturado manufacturado) {
                manufacturado.liberarStock(cantTotal);
            } else if (articulo instanceof ArticuloInsumo insumo) {
                double requerido = insumo.convertirACantidadBase(cantTotal);
                insumo.liberarStock(requerido);
            }
        }
    }

    public void confirmarStock(double cantidad) {
        for (PromocionDetalle detalle : this.getPromocionDetalle()) {
            Articulo articulo = detalle.getArticulos();
            double cantTotal = detalle.getCantidad() * cantidad;

            if (articulo instanceof ArticuloManufacturado manufacturado) {
                manufacturado.confirmarStock(cantTotal);
            } else if (articulo instanceof ArticuloInsumo insumo) {
                double requerido = insumo.convertirACantidadBase(cantTotal);
                insumo.confirmarStock(requerido);
            }
        }
    }
}


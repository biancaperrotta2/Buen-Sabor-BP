package com.aluracursos.buensaborbp.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
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

    @Builder.Default
    @OneToMany(mappedBy = "articuloPromocion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromocionDetalle> promocionDetalle = new ArrayList<>();

    /**
     * Imágenes específicas de la promoción.
     * Se usa un nombre distinto para evitar colisión con el campo
     * {@code imagenes} heredado de {@link Articulo}.
     */
    @Builder.Default
    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImagenPromocion> imagenesPromocion = new ArrayList<>();

    // ================== MÉTODOS DE CÁLCULO ================== //
    public void calcularPrecioCosto() {
        BigDecimal costoTotal = BigDecimal.ZERO;

        if (promocionDetalle == null || promocionDetalle.isEmpty()) {
            throw new IllegalStateException("La promoción debe tener al menos un detalle para calcular el costo");
        }

        for (PromocionDetalle detalle : promocionDetalle) {
            Articulo articulo = detalle.getArticulos();
            if (articulo == null) {
                throw new IllegalStateException("El detalle de promoción debe tener un artículo asociado");
            }
            
            BigDecimal cantidad = BigDecimal.valueOf(detalle.getCantidad());

            if (articulo instanceof ArticuloInsumo insumo) {
                // Si es un insumo, convertimos la cantidad a la unidad base
                BigDecimal precioCosto = insumo.getPrecioCosto();
                if (precioCosto == null) {
                    throw new IllegalStateException("El insumo " + insumo.getNombre() + " no tiene precio de costo definido");
                }
                BigDecimal cantidadBase = BigDecimal.valueOf(insumo.convertirACantidadBase(cantidad.doubleValue()));
                BigDecimal precioPorCantidad = precioCosto.multiply(cantidadBase);
                costoTotal = costoTotal.add(precioPorCantidad);
            } else {
                // Si es manufacturado o promoción, multiplicamos costo por cantidad
                BigDecimal precioCosto = articulo.getPrecioCosto();
                if (precioCosto == null) {
                    throw new IllegalStateException("El artículo " + articulo.getNombre() + " no tiene precio de costo definido");
                }
                BigDecimal precioPorCantidad = precioCosto.multiply(cantidad);
                costoTotal = costoTotal.add(precioPorCantidad);
            }
        }
        setPrecioCosto(costoTotal);
    }

    public void calcularPrecioTotal() {
        BigDecimal ventaTotal = BigDecimal.ZERO;

        if (promocionDetalle == null || promocionDetalle.isEmpty()) {
            throw new IllegalStateException("La promoción debe tener al menos un detalle para calcular el precio total");
        }

        for (PromocionDetalle detalle : this.getPromocionDetalle()) {
            Articulo articulo = detalle.getArticulos();
            if (articulo == null) {
                throw new IllegalStateException("El detalle de promoción debe tener un artículo asociado");
            }
            
            BigDecimal precio = articulo.getPrecioVenta();
            if (precio == null) {
                throw new IllegalStateException("El artículo " + articulo.getNombre() + " no tiene precio de venta definido");
            }
            
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


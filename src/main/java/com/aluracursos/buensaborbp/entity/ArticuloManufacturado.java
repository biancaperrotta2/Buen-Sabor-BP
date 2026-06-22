package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("ARTICULO_MANUFACTURADO")
public class ArticuloManufacturado extends Articulo{
    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String preparacion;

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_manufacturado_id")
    private List<ArticuloManufacturadoDetalle> articuloManufacturadoDetalles = new ArrayList<>();

    public void tiempoEstimadoMinutos(int tiempoBase) {
        if (articuloManufacturadoDetalles == null || articuloManufacturadoDetalles.isEmpty()) {
            setTiempoEstimadoMinutos(tiempoBase);
            return;
        }

        int tiempoDetalle = 0;
        for (ArticuloManufacturadoDetalle detalle : articuloManufacturadoDetalles) {
            if (detalle != null && detalle.getArticuloInsumo() != null) {
                Integer tiempoInsumo = detalle.getArticuloInsumo().getTiempoEstimadoMinutos();
                if (tiempoInsumo != null) {
                    tiempoDetalle += tiempoInsumo;
                }
            }
        }
        setTiempoEstimadoMinutos(tiempoBase + tiempoDetalle);
    }

    public void calcularPrecioCosto(){
        if (articuloManufacturadoDetalles == null || articuloManufacturadoDetalles.isEmpty()) {
            setPrecioCosto(BigDecimal.ZERO);
            return;
        }

        BigDecimal costoTotal = BigDecimal.ZERO;
        for (ArticuloManufacturadoDetalle detalle : articuloManufacturadoDetalles) {
            if (detalle == null || detalle.getArticuloInsumo() == null) {
                continue;
            }
            
            ArticuloInsumo insumo = detalle.getArticuloInsumo();
            BigDecimal precioCostoUnitario = insumo.getPrecioCosto();
            
            if (precioCostoUnitario == null) {
                throw new IllegalStateException("El insumo " + insumo.getNombre() + " no tiene precio de costo definido.");
            }
            
            BigDecimal cantidadBase = BigDecimal.valueOf(insumo.convertirACantidadBase(detalle.getCantidad()));
            BigDecimal precioPorCantidad = precioCostoUnitario.multiply(cantidadBase);

            costoTotal = costoTotal.add(precioPorCantidad);
        }
        setPrecioCosto(costoTotal);
    }

    public void calcularPrecioVenta(BigDecimal precioVentaManual){
        if (precioVentaManual == null || precioVentaManual.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("El precio de venta debe ser mayor a cero.");
        }else{
            setPrecioVenta(precioVentaManual);
        }
    }

    public void reservarStock(double cantidad, String articuloPadreNombre) {
        for (ArticuloManufacturadoDetalle detalle : this.getArticuloManufacturadoDetalles()) {
            ArticuloInsumo insumo = detalle.getArticuloInsumo();
            double requeridoBase = insumo.convertirACantidadBase(detalle.getCantidad() * cantidad);
            insumo.reservarStock(requeridoBase, articuloPadreNombre);
        }
    }

    public void liberarStock(double cantidad) {
        for (ArticuloManufacturadoDetalle detalle : this.getArticuloManufacturadoDetalles()) {
            ArticuloInsumo insumo = detalle.getArticuloInsumo();
            double requeridoBase = insumo.convertirACantidadBase(detalle.getCantidad() * cantidad);
            insumo.liberarStock(requeridoBase);
        }
    }

    public void confirmarStock(double cantidad) {
        for (ArticuloManufacturadoDetalle detalle : this.getArticuloManufacturadoDetalles()) {
            ArticuloInsumo insumo = detalle.getArticuloInsumo();
            double requeridoBase = insumo.convertirACantidadBase(detalle.getCantidad() * cantidad);
            insumo.confirmarStock(requeridoBase);
        }
    }

      
}




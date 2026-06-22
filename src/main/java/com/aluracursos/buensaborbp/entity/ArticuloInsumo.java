package com.aluracursos.buensaborbp.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import com.aluracursos.buensaborbp.exception.StockInsuficienteException;
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
@DiscriminatorValue("ARTICULO_INSUMO")
public class ArticuloInsumo extends Articulo{
    @Column(nullable = false)
    private Double stockActual;

    @Column(nullable = false)
    private Double stockMaximo;

    @Column(nullable = false)
    private Double stockMinimo;

    @Builder.Default
    private Double stockReservado = 0.0; //stock apartado para pedidos aun no confirmados/pagados

    public Double getStockDisponible(){
        return stockActual - stockReservado;
    }

    //stock reservado cuando el cliente aun no paga el pedido, en el proceso de seleccion de productos se va reservando el stock segun lo q desee. Si hay stock disponible se RESERVA sino lanzamos ERROR.
    public void reservarStock(double cantidad, String articuloNombre){
        double cantidadBase = convertirACantidadBase(cantidad);
        if(getStockDisponible() >= cantidadBase){
            this.stockReservado += cantidadBase;
        } else {
            throw new StockInsuficienteException("No contamos con stock suficiente del insumo " + this.getNombre() + " requerido por el artículo " + articuloNombre + ". Favor de disminuir la cantidad o esperar reposición. Gracias");
        }
    }

    //si el pedido se cancela, anulamos el stock de articulos reservados, restamos: stock reservado - cantidad reservada 
    public void liberarStock(double cantidad) {
        double cantidadBase = convertirACantidadBase(cantidad);
        this.stockReservado = Math.max(0, this.stockReservado - cantidadBase);
    }

    //si el pedido se concreta, hacemos efectivo el descuento de stock: stock actual - cantidad y reiniciamos el stock reservado dejandolo libre.
    public void confirmarStock(double cantidad) {
        double cantidadBase = convertirACantidadBase(cantidad);
        if (this.stockReservado < cantidadBase) {
            throw new IllegalStateException("No hay suficiente stock reservado para confirmar.");
        }
        this.stockActual -= cantidadBase;
        this.stockReservado -= cantidadBase;
    }

    public void calcularPrecioVenta(BigDecimal precioVentaManual) {
        if (this.getMargen() != null && this.getPrecioCosto() != null) {
            if (this.getMargen() == 0f) {
                // Margen explícitamente 0%
                this.setPrecioVenta(this.getPrecioCosto());
            } else {
                // Calcular el precio con el margen
                BigDecimal margen = BigDecimal.valueOf(this.getMargen()).divide(BigDecimal.valueOf(100));
                BigDecimal nuevoPrecioVenta = this.getPrecioCosto().multiply(BigDecimal.ONE.add(margen));
                this.setPrecioVenta(nuevoPrecioVenta);
            }
        } else {
            // Si no hay margen, usar el precio ingresado manualmente
            this.setPrecioVenta(precioVentaManual);
        }
    }

    public double convertirACantidadBase(double cantidad) {
        if (getUnidadMedidaEnum() == null) {
            throw new IllegalStateException("La unidad de medida no está definida para el insumo: " + this.getNombre());
        }
        return cantidad * getUnidadMedidaEnum().getFactor();
    }

/**
 * Devuelve true si este insumo tiene stock igual o por debajo del stock mínimo.
 */
public boolean tieneStockMinimo() {
    return this.stockActual <= this.stockMinimo;
}

/**
 * Dada una lista de insumos, devuelve solo aquellos que están en stock mínimo o por debajo.
 */
public static List<ArticuloInsumo> obtenerInsumosConStockMinimo(List<ArticuloInsumo> insumos) {
    List<ArticuloInsumo> insumosConStockMinimo = new ArrayList<>();
    if (insumos != null) {
        for (ArticuloInsumo insumo : insumos) {
            if (insumo != null && insumo.tieneStockMinimo()) {
                insumosConStockMinimo.add(insumo);
            }
        }
    }
    return insumosConStockMinimo;
}

}

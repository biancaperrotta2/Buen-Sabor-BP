package com.aluracursos.buensaborbp.entity;

import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import com.aluracursos.buensaborbp.exception.StockInsuficienteException;
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
@DiscriminatorValue("ARTICULO_INSUMO")
public class ArticuloInsumo extends Articulo{
    @Column(nullable = false)
    private Double stockActual;

    @Column(nullable = false)
    private Double stockMaximo;

    @Column(nullable = false)
    private Double stockMinimo;

    @Builder.Default
    private Double stockReservado = 0.0; //Se pone en 0 para todos los nuevos insumos

    public Double getStockDisponible(){
        return stockActual - stockReservado;
    }

    public void reservarStock(double cantidad, String articuloNombre){
        double cantidadBase = convertirACantidadBase(cantidad);
        if(getStockDisponible() > cantidadBase){
            this.stockReservado += cantidadBase;
        }else {
            throw new StockInsuficienteException("No contamos con stock suficiente del insumo " + this.getNombre() + " requerido por el articulo " + articuloNombre + ". Favor de disminuir la cantidad o esperar reposición. Gracias");
        }
    }

    public void liberarStock(double cantidad) {
        double cantidadBase = convertirACantidadBase(cantidad);
        this.stockReservado = Math.max(0, this.stockReservado - cantidadBase);
    }

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
        return cantidad * unidadMedidaEnum.getFactor();
    }


}

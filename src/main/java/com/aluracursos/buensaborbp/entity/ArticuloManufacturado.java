package com.aluracursos.buensaborbp.entity;

import com.aluracursos.buensaborbp.exception.StockInsuficienteException;
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

    // Un semielaborado (ej. "masa de pizza") es un ArticuloManufacturado más: tiene receta
    // propia (para costearlo) Y stock propio (se prepara en lote y se consume de a poco),
    // a diferencia de un plato final, que nunca tiene stock propio y siempre cascada a sus
    // componentes. Los campos de stock de abajo solo tienen sentido cuando esto es true.
    @Builder.Default
    @Column(nullable = false)
    private Boolean esSemielaborado = false;

    private Double stockActual;
    private Double stockMinimo;
    private Double stockMaximo;

    @Builder.Default
    private Double stockReservado = 0.0;

    @Builder.Default
    @OneToMany(mappedBy = "articuloManufacturado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticuloManufacturadoDetalle> articuloManufacturadoDetalles = new ArrayList<>();

    public Double getStockDisponible() {
        return stockActual - stockReservado;
    }

    public boolean tieneStockMinimo() {
        return this.stockActual != null && this.stockMinimo != null && this.stockActual <= this.stockMinimo;
    }

    public void tiempoEstimadoMinutos(int tiempoBase) {
        if (articuloManufacturadoDetalles == null || articuloManufacturadoDetalles.isEmpty()) {
            setTiempoEstimadoMinutos(tiempoBase);
            return;
        }

        int tiempoDetalle = 0;
        for (ArticuloManufacturadoDetalle detalle : articuloManufacturadoDetalles) {
            if (detalle != null && detalle.getComponente() != null) {
                Integer tiempoComponente = detalle.getComponente().getTiempoEstimadoMinutos();
                if (tiempoComponente != null) {
                    tiempoDetalle += tiempoComponente;
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
            if (detalle == null || detalle.getComponente() == null) {
                continue;
            }

            Articulo componente = detalle.getComponente();
            BigDecimal precioCostoUnitario = componente.getPrecioCosto();

            if (precioCostoUnitario == null) {
                throw new IllegalStateException("El componente " + componente.getNombre() + " no tiene precio de costo definido.");
            }

            BigDecimal cantidadBase = BigDecimal.valueOf(componente.convertirACantidadBase(detalle.getCantidad()));
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

    // Semielaborado: se comporta como un ArticuloInsumo (ajusta su propio stock).
    // Plato final: cascada a cada componente de la receta, que resuelve polimórficamente
    // a un insumo o a otro semielaborado (y ahí termina la cascada, no sigue bajando).
    public void reservarStock(double cantidad, String articuloPadreNombre) {
        if (Boolean.TRUE.equals(esSemielaborado)) {
            double cantidadBase = convertirACantidadBase(cantidad);
            if (getStockDisponible() >= cantidadBase) {
                this.stockReservado += cantidadBase;
            } else {
                throw new StockInsuficienteException("No contamos con stock suficiente del semielaborado " + this.getNombre()
                        + " requerido por el artículo " + articuloPadreNombre + ". Favor de disminuir la cantidad o esperar reposición. Gracias");
            }
            return;
        }
        for (ArticuloManufacturadoDetalle detalle : this.getArticuloManufacturadoDetalles()) {
            Articulo componente = detalle.getComponente();
            componente.reservarStock(detalle.getCantidad() * cantidad, articuloPadreNombre);
        }
    }

    public void liberarStock(double cantidad) {
        if (Boolean.TRUE.equals(esSemielaborado)) {
            double cantidadBase = convertirACantidadBase(cantidad);
            this.stockReservado = Math.max(0, this.stockReservado - cantidadBase);
            return;
        }
        for (ArticuloManufacturadoDetalle detalle : this.getArticuloManufacturadoDetalles()) {
            Articulo componente = detalle.getComponente();
            componente.liberarStock(detalle.getCantidad() * cantidad);
        }
    }

    public void confirmarStock(double cantidad) {
        if (Boolean.TRUE.equals(esSemielaborado)) {
            double cantidadBase = convertirACantidadBase(cantidad);
            if (this.stockReservado < cantidadBase) {
                throw new IllegalStateException("No hay suficiente stock reservado para confirmar.");
            }
            this.stockActual -= cantidadBase;
            this.stockReservado -= cantidadBase;
            return;
        }
        for (ArticuloManufacturadoDetalle detalle : this.getArticuloManufacturadoDetalles()) {
            Articulo componente = detalle.getComponente();
            componente.confirmarStock(detalle.getCantidad() * cantidad);
        }
    }
}




package com.aluracursos.buensaborbp.entity;

import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@DiscriminatorColumn(name = "DTYPE")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Articulo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private BigDecimal precioVenta;

    private BigDecimal precioCosto;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    private Boolean esVendible;
    private Integer tiempoEstimadoMinutos;

    @Builder.Default
    @OneToMany(mappedBy = "articulo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ImagenArticulo> imagenes = new ArrayList<>();

    @ManyToOne()
    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "unidad_medidaEnum_id")
     UnidadMedidaEnum unidadMedidaEnum;
    
    @ManyToOne()
    @JoinColumn(name = "unidad_medida_id")
    private UnidadMedida unidadMedida;

    @ManyToOne()
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "articulo_promocion",
            joinColumns = @JoinColumn(name = "articulo_id"),
            inverseJoinColumns = @JoinColumn(name = "promocion_id")
    )
    private List<Promocion> promociones = new ArrayList<>();

    private Float margen;
    
    
    public Long getId(){
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public BigDecimal getPrecioCosto() {
        return precioCosto;
    }

    public void setPrecioCosto(BigDecimal precioCosto) {
        this.precioCosto = precioCosto;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getEsVendible() {
        return esVendible;
    }

    public void setEsVendible(Boolean esVendible) {
        this.esVendible = esVendible;
    }

    public Integer getTiempoEstimadoMinutos() {
        return tiempoEstimadoMinutos;
    }

    public void setTiempoEstimadoMinutos(Integer tiempoEstimadoMinutos) {
        this.tiempoEstimadoMinutos = tiempoEstimadoMinutos;
    }

    public List<ImagenArticulo> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<ImagenArticulo> imagenes) {
        this.imagenes = imagenes;
    }

    public UnidadMedidaEnum getUnidadMedidaEnum() {
        return unidadMedidaEnum;
    }

    public void setUnidadMedidaEnum(UnidadMedidaEnum unidadMedidaEnum) {
        this.unidadMedidaEnum = unidadMedidaEnum;
    }

    public UnidadMedida getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(UnidadMedida unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public List<Promocion> getPromociones() {
        return promociones;
    }

    public void setPromociones(List<Promocion> promociones) {
        this.promociones = promociones;
    }

    public Float getMargen() {
        return margen;
    }

    //get y set
    public void setMargen(Float margen) {    
        this.margen = margen;
    }

    public void activar() {
        this.activo = true;
        this.esVendible = true;
    }

    public void desactivar(){
        this.activo = false;
        this.esVendible = false;
    }

    //metodos abstractos
    public abstract void reservarStock(double cantidad, String articuloPadreNombre);

    public abstract void liberarStock(double cantidad);

    public abstract void confirmarStock(double cantidad);


}

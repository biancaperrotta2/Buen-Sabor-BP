package com.aluracursos.buensaborbp.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
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

    /**
     * Indica si el artículo se puede vender al cliente final.
     * En insumos internos suele ser false, en manufacturados y vendibles true.
     */
    private Boolean esVendible;

    /**
     * Tiempo estimado de preparación del artículo en minutos.
     * Convención:
     *  - Manufacturados: tiempo real de preparación (incluye lógica específica en subclases).
     *  - Insumos / productos sin preparación (bebidas, empaquetados): 0.
     *  - Promociones: puede usarse el máximo/suma de sus componentes, o dejar 0 si se calcula aparte.
     */
    @Builder.Default
    private Integer tiempoEstimadoMinutos = 0;

    @Builder.Default
    @OneToMany(mappedBy = "articulo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ImagenArticulo> imagenes = new ArrayList<>();

    /**
     * Unidad de medida lógica del artículo, basada en un catálogo fijo
     * definido en el backend. Se persiste como texto (EnumType.STRING).
     *
     * Ejemplos: GR, KG, ML, L, UNIDAD.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false)
    private UnidadMedidaEnum unidadMedidaEnum;

    @ManyToOne(fetch = FetchType.LAZY)
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

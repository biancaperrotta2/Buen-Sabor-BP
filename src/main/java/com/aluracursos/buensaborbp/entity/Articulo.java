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

    @ManyToOne(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "unidad_medidaEnum_id")
    private UnidadMedidaEnum unidadMedidaEnum;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_medida_id")
    private UnidadMedida unidadMedida;

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

package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String razonSocial;

    @Column(nullable = false)
    private Integer cuit;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true )
    private List<Sucursal> listaSucursal;
}

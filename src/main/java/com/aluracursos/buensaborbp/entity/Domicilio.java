package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Domicilio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String calle;
    private String numero; 
    private String cp;     
    private String piso;
    private String nroDepto;
    private String aclaraciones;
    private String alias;
    private boolean activo;

    @ManyToOne
    @JoinColumn(name = "id_ciudad")
    private Ciudad ciudad;
}
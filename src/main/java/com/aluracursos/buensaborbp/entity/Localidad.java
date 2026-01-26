package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Localidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provincia_id" , referencedColumnName = "id", nullable = false)
    private Provincia provincia;

    @Override
    public String toString() {
        return "Localidad" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", provincia=" + provincia;
    }
}

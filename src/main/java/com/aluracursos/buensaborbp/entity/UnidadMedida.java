package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UnidadMedida extends Base {

    @Column(nullable = false, unique = true)
    private String denominacion; // Ej: "Gramos", "Kilos", "Litros", "Unidad"

    @Column(nullable = false)
    private String abreviatura; // Ej: "gr", "kg", "lt", "un"
}

package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Empleado extends Usuario {
    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}





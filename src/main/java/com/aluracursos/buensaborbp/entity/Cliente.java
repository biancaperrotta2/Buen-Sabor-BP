package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Cliente extends Usuario{
    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "cliente_domicilio",
            joinColumns = @JoinColumn(
                    name = "cliente_id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "domicilio_id"
            )
    )
    private List<Domicilio> domicilio;

    @OneToMany(mappedBy = "cliente")
    private List<Pedido> listaPedido;
}

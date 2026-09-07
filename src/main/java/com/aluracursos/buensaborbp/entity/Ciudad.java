package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ciudad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    // Costo fijo de envío a esta ciudad. Nullable porque las filas existentes antes de
    // este campo no tienen valor migrado; se trata como 0 dondequiera que se use.
    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal costoEnvio = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "provincia_id", nullable = false)
    private Provincia provincia;

    @OneToMany(mappedBy = "ciudad", cascade = CascadeType.ALL)
    private List<Domicilio> domicilios;
}
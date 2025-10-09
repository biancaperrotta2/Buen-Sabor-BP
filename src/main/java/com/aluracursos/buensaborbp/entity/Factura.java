package com.aluracursos.buensaborbp.entity;

import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.FormaPago;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_factura;

    @Column(nullable = false)
    private LocalDate fechaFacturacion;

    @Embedded
    private DetalleMercadoPago detalleMercadoPago;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Estado estadoPago;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FormaPago formaPago;

    @Column(nullable = false)
    private Double totalVenta;

    @Builder.Default
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleFactura> detalleFactura = new ArrayList<>();
}

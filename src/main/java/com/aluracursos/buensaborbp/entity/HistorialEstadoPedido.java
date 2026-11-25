package com.aluracursos.buensaborbp.entity;

import com.aluracursos.buensaborbp.entity.Enums.Estado;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// Cada cambio de estado de un pedido: qué estado, cuándo y quién (enunciado, diálogo de Órdenes
// y Cocina, sección "Estado de la orden": Estado / Actualizado el / Actualizado por). Se registra
// desde el 2026-11-24; los pedidos anteriores no tienen historial.
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistorialEstadoPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado;

    @Column(nullable = false)
    private LocalDateTime fecha;

    // Nombre del empleado o cliente que lo cambió; "Sistema" si fue automático (ej. pago vencido).
    @Column(nullable = false)
    private String usuario;
}

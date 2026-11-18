package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Orden de la pasarela cripto simulada (cripto.proveedor=simulado). Hace las veces de lo que
// guardaría CoinGate de su lado: no mueve dinero, solo permite recorrer el circuito de pago.
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrdenCriptoSimulada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long idPedido;

    @Column(nullable = false)
    private BigDecimal montoUsd;

    // Mismos nombres que CoinGate: new, paid, expired, canceled.
    @Column(nullable = false, length = 30)
    private String estado;

    private String token;

    private String callbackUrl;
    private String successUrl;
    private String cancelUrl;

    // Moneda que el cliente eligió al simular el pago (BTC, ETH, USDT, USDC); solo informativa.
    @Column(length = 10)
    private String moneda;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;
}

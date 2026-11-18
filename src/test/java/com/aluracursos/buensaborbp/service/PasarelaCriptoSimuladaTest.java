package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.OrdenCriptoSimulada;
import com.aluracursos.buensaborbp.repository.OrdenCriptoSimuladaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasarelaCriptoSimuladaTest {

    @Mock private OrdenCriptoSimuladaRepository repository;

    private PasarelaCriptoSimulada pasarela;

    @BeforeEach
    void setUp() {
        pasarela = new PasarelaCriptoSimulada(repository, "http://localhost:5173/pago/cripto-simulado/");
    }

    private static OrdenCriptoSimulada orden(String estado) {
        return OrdenCriptoSimulada.builder().id(5L).idPedido(1L).montoUsd(new BigDecimal("9.00"))
                .estado(estado).token("t").fechaCreacion(LocalDateTime.now()).build();
    }

    @Test
    void crearOrden_quedaNuevaConElLinkDeLaPaginaSimulada() {
        when(repository.save(any())).thenAnswer(inv -> {
            OrdenCriptoSimulada o = inv.getArgument(0);
            o.setId(5L);
            return o;
        });

        PasarelaCripto.OrdenCripto orden = pasarela.crearOrden(Map.of(
                "order_id", "1", "price_amount", "9.00", "token", "t", "success_url", "http://x/exito"));

        assertThat(orden.status()).isEqualTo("new");
        assertThat(orden.orderId()).isEqualTo("1");
        assertThat(orden.paymentUrl()).isEqualTo("http://localhost:5173/pago/cripto-simulado/5");
    }

    @Test
    void simularPago_cambiaElEstado() {
        when(repository.findById(5L)).thenReturn(Optional.of(orden("new")));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdenCriptoSimulada o = pasarela.simular(5L, "paid", "USDT");

        assertThat(o.getEstado()).isEqualTo("paid");
        assertThat(o.getMoneda()).isEqualTo("USDT");
    }

    @Test
    void unaOrdenTerminadaNoSePuedeVolverASimular() {
        when(repository.findById(5L)).thenReturn(Optional.of(orden("paid")));

        assertThrows(IllegalStateException.class, () -> pasarela.simular(5L, "expired", null));
    }

    @Test
    void ordenInexistente() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(PagoInexistenteException.class, () -> pasarela.obtenerOrden(9L));
    }
}

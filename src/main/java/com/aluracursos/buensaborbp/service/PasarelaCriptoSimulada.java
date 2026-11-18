package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.OrdenCriptoSimulada;
import com.aluracursos.buensaborbp.repository.OrdenCriptoSimuladaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

// Pasarela cripto simulada (por defecto). Las registraciones a los sandbox de CoinGate y
// NOWPayments no estaban disponibles, así que la usuaria eligió simularla: guarda la orden como
// lo haría la pasarela y su página de pago (frontend /pago/cripto-simulado/{id}) tiene botones
// para simular el pago aprobado o vencido. No mueve dinero.
@Component
@ConditionalOnProperty(name = "cripto.proveedor", havingValue = "simulado", matchIfMissing = true)
public class PasarelaCriptoSimulada implements PasarelaCripto {

    private static final Set<String> ESTADOS_FINALES = Set.of("paid", "expired", "canceled");

    private final OrdenCriptoSimuladaRepository repository;
    private final String urlPago;

    public PasarelaCriptoSimulada(OrdenCriptoSimuladaRepository repository,
                                  @Value("${cripto.simulado.url-pago:http://localhost:5173/pago/cripto-simulado/}") String urlPago) {
        this.repository = repository;
        this.urlPago = urlPago;
    }

    @Override
    @Transactional
    public OrdenCripto crearOrden(Map<String, String> campos) {
        OrdenCriptoSimulada orden = repository.save(OrdenCriptoSimulada.builder()
                .idPedido(Long.valueOf(campos.get("order_id")))
                .montoUsd(new BigDecimal(campos.get("price_amount")))
                .estado("new")
                .token(campos.get("token"))
                .callbackUrl(campos.get("callback_url"))
                .successUrl(campos.get("success_url"))
                .cancelUrl(campos.get("cancel_url"))
                .fechaCreacion(LocalDateTime.now())
                .build());
        return aOrden(orden);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenCripto obtenerOrden(Long idOrden) {
        return aOrden(buscar(idOrden));
    }

    // Lo que hace el cliente en la página de pago simulada. Devuelve la orden ya actualizada.
    @Transactional
    public OrdenCriptoSimulada simular(Long idOrden, String nuevoEstado, String moneda) {
        OrdenCriptoSimulada orden = buscar(idOrden);
        if (ESTADOS_FINALES.contains(orden.getEstado())) {
            throw new IllegalStateException("Esta orden de pago ya terminó (" + orden.getEstado() + ")");
        }
        orden.setEstado(nuevoEstado);
        if (moneda != null && !moneda.isBlank()) {
            orden.setMoneda(moneda);
        }
        return repository.save(orden);
    }

    @Transactional(readOnly = true)
    public OrdenCriptoSimulada buscar(Long idOrden) {
        return repository.findById(idOrden).orElseThrow(() -> new PagoInexistenteException(idOrden));
    }

    private OrdenCripto aOrden(OrdenCriptoSimulada o) {
        return new OrdenCripto(o.getId(), o.getEstado(), o.getIdPedido().toString(), urlPago + o.getId());
    }
}

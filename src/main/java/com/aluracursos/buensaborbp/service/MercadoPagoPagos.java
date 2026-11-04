package com.aluracursos.buensaborbp.service;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPSearchRequest;
import com.mercadopago.resources.payment.Payment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

// Consultas de pagos a la API de Mercado Pago. Separado del servicio para que la lógica que
// decide qué hacer con un pago (PedidoServiceImpl) se pueda testear sin llamar a MP.
@Component
public class MercadoPagoPagos {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MercadoPagoPagos.class);

    // Lanza PagoInexistenteException si MP responde que ese pago no existe (404), para que el
    // webhook pueda distinguirlo de una falla de MP (que sí conviene que MP reintente).
    public Payment obtener(Long paymentId) {
        try {
            return new PaymentClient().get(paymentId);
        } catch (MPApiException e) {
            if (e.getStatusCode() == 404) {
                throw new PagoInexistenteException(paymentId);
            }
            throw new IllegalStateException("No se pudo consultar el pago " + paymentId + " en Mercado Pago: "
                    + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            throw new IllegalStateException("No se pudo consultar el pago " + paymentId + " en Mercado Pago: " + e.getMessage(), e);
        }
    }

    // Todos los pagos que MP tiene para un pedido (la preferencia guarda el id del pedido como
    // external_reference). Lista vacía si el cliente todavía no pagó.
    public List<Payment> buscarPorPedido(Long pedidoId) {
        // limit/offset explícitos: el SDK 3.7 arma la URL con todos los parámetros y si quedan en
        // null tira NullPointerException al formatearla.
        MPSearchRequest busqueda = MPSearchRequest.builder()
                .limit(30)
                .offset(0)
                .filters(Map.of("external_reference", pedidoId.toString()))
                .build();
        try {
            List<Payment> resultados = new PaymentClient().search(busqueda).getResults();
            if (resultados == null) return List.of();
            // No confiar solo en el filtro de MP: se comprobó que puede devolver pagos de otros
            // pedidos, y tomar un aprobado ajeno marcaría como pagado un pedido que no lo está.
            List<Payment> delPedido = resultados.stream()
                    .filter(p -> pedidoId.toString().equals(p.getExternalReference()))
                    .toList();
            if (delPedido.size() != resultados.size()) {
                log.warn("Búsqueda de pagos del pedido {}: MP devolvió {} pagos, {} de otros pedidos (descartados)",
                        pedidoId, resultados.size(), resultados.size() - delPedido.size());
            }
            log.info("Pagos en MP del pedido {}: {}", pedidoId, delPedido.stream()
                    .map(p -> p.getId() + "=" + p.getStatus() + "@" + p.getDateCreated())
                    .toList());
            return delPedido;
        } catch (MPApiException e) {
            throw new IllegalStateException("No se pudo consultar los pagos del pedido " + pedidoId + " en Mercado Pago: "
                    + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            throw new IllegalStateException("No se pudo consultar los pagos del pedido " + pedidoId + " en Mercado Pago: " + e.getMessage(), e);
        }
    }
}

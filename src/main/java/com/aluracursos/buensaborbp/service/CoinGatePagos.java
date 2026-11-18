package com.aluracursos.buensaborbp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

// Llamadas a la API de CoinGate (pago con criptomonedas). Separado del servicio, igual que
// MercadoPagoPagos, para que la lógica de qué hacer con un pago se pueda testear sin llamar a
// CoinGate. No hay SDK oficial para Java: se usa la API REST v2 directamente.
@Component
@ConditionalOnProperty(name = "cripto.proveedor", havingValue = "coingate")
public class CoinGatePagos implements PasarelaCripto {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CoinGatePagos.class);

    private final String apiToken;
    private final boolean sandbox;
    private final ObjectMapper objectMapper;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    public CoinGatePagos(@Value("${coingate.api-token:}") String apiToken,
                         @Value("${coingate.sandbox:true}") boolean sandbox,
                         ObjectMapper objectMapper) {
        this.apiToken = apiToken;
        this.sandbox = sandbox;
        this.objectMapper = objectMapper;
    }

    // El sandbox (sandbox.coingate.com) es una cuenta y un token aparte del de producción.
    private String baseUrl() {
        return sandbox ? "https://api-sandbox.coingate.com/api/v2" : "https://api.coingate.com/api/v2";
    }

    @Override
    public OrdenCripto crearOrden(Map<String, String> campos) {
        String formulario = campos.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isBlank())
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        HttpRequest pedido = base("/orders")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formulario))
                .build();
        OrdenCripto orden = leerOrden(enviar(pedido, "crear la orden de pago"));
        log.info("Orden de CoinGate {} creada para el pedido {} (status {})", orden.id(), orden.orderId(), orden.status());
        return orden;
    }

    // Lanza PagoInexistenteException si CoinGate responde que la orden no existe (404).
    @Override
    public OrdenCripto obtenerOrden(Long idOrden) {
        HttpRequest pedido = base("/orders/" + idOrden).GET().build();
        HttpResponse<String> respuesta = enviarSinValidar(pedido, "consultar la orden " + idOrden);
        if (respuesta.statusCode() == 404) {
            throw new PagoInexistenteException(idOrden);
        }
        return leerOrden(validar(respuesta, "consultar la orden " + idOrden));
    }

    private HttpRequest.Builder base(String ruta) {
        if (apiToken == null || apiToken.isBlank()) {
            throw new IllegalStateException("El pago con criptomonedas no está configurado (falta coingate.api-token).");
        }
        return HttpRequest.newBuilder(URI.create(baseUrl() + ruta))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .header("Authorization", "Token " + apiToken);
    }

    private String enviar(HttpRequest pedido, String accion) {
        return validar(enviarSinValidar(pedido, accion), accion);
    }

    private HttpResponse<String> enviarSinValidar(HttpRequest pedido, String accion) {
        try {
            return http.send(pedido, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo " + accion + " en CoinGate: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Se interrumpió la llamada a CoinGate al " + accion, e);
        }
    }

    private String validar(HttpResponse<String> respuesta, String accion) {
        if (respuesta.statusCode() / 100 != 2) {
            throw new IllegalStateException("CoinGate no pudo " + accion + " (HTTP " + respuesta.statusCode() + "): " + respuesta.body());
        }
        return respuesta.body();
    }

    private OrdenCripto leerOrden(String cuerpo) {
        try {
            JsonNode n = objectMapper.readTree(cuerpo);
            return new OrdenCripto(
                    n.path("id").isMissingNode() || n.path("id").isNull() ? null : n.path("id").asLong(),
                    n.path("status").asText(null),
                    n.path("order_id").asText(null),
                    n.path("payment_url").asText(null));
        } catch (IOException e) {
            throw new IllegalStateException("Respuesta inesperada de CoinGate: " + cuerpo, e);
        }
    }
}

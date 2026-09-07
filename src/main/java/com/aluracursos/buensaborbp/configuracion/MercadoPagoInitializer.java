package com.aluracursos.buensaborbp.configuracion;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// A diferencia del secret de JWT, que si falta impide levantar la app, un Mercado Pago sin
// configurar solo debe romper las funcionalidades de pago (todavía en desarrollo/sin cuenta
// de test creada) — el resto del backend tiene que seguir funcionando igual.
@Component
public class MercadoPagoInitializer {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoInitializer.class);

    private final String accessToken;

    public MercadoPagoInitializer(@Value("${mercadopago.access-token:}") String accessToken) {
        this.accessToken = accessToken;
    }

    @PostConstruct
    public void init() {
        if (accessToken == null || accessToken.isBlank() || accessToken.equals("TEST-COMPLETAR")) {
            log.warn("Mercado Pago sin configurar (falta mercadopago.access-token en application-local.properties). "
                    + "Los endpoints de /pagos van a fallar hasta que se complete.");
            return;
        }
        MercadoPagoConfig.setAccessToken(accessToken);
    }
}

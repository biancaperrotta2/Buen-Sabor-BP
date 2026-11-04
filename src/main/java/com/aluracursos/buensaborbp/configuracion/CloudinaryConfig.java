package com.aluracursos.buensaborbp.configuracion;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    // Sin default fallaría el arranque de quien no tenga las claves; así solo falla la subida
    // de fotos (con un 502 claro), igual que Mercado Pago con sus claves.
    @Bean
    public Cloudinary cloudinary(
            @Value("${cloudinary.cloud_name:}") String cloudName,
            @Value("${cloudinary.api_key:}") String apiKey,
            @Value("${cloudinary.api_secret:}") String apiSecret) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", "true");
        return new Cloudinary(config);
    }
}

package com.aluracursos.buensaborbp.configuracion;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

// Inicializa el SDK de administración de Firebase (usado para la recuperación de
// contraseña: crear el usuario espejo al registrarse y verificar el idToken que
// devuelve Firebase después del login/reseteo). La clave de la cuenta de servicio
// es secreta y no se versiona (ver .gitignore).
@Configuration
public class FirebaseConfig {

    @Bean
    FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }
        try (InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            return FirebaseApp.initializeApp(options);
        }
    }
}

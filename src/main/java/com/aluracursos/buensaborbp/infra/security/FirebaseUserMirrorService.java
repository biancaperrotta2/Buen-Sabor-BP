package com.aluracursos.buensaborbp.infra.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// Crea, para cada Cliente que se registra con email/contraseña, un usuario espejo en
// Firebase Authentication con la misma contraseña. Es lo que permite que exista una
// cuenta ahí para cuando el cliente use "¿Olvidaste tu contraseña?" (Firebase no puede
// mandar un mail de reseteo a un usuario que no tiene registrado).
@Service
public class FirebaseUserMirrorService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseUserMirrorService.class);

    // Si falla (ej. el email ya existe en Firebase por otra causa), no debe romper el
    // registro local: la cuenta espejo se puede reintentar/crear después si hace falta.
    public String crearEspejo(String email, String password, String nombreCompleto) {
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setDisplayName(nombreCompleto)
                    .setEmailVerified(false)
                    .setDisabled(false);
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            return userRecord.getUid();
        } catch (FirebaseAuthException e) {
            log.warn("No se pudo crear el usuario espejo en Firebase para {}: {}", email, e.getMessage());
            return null;
        }
    }
}

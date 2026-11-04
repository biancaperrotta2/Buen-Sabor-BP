package com.aluracursos.buensaborbp.infra.security;

import com.aluracursos.buensaborbp.entity.Cliente;
import com.aluracursos.buensaborbp.entity.Enums.Rol;
import com.aluracursos.buensaborbp.entity.Usuario;
import com.aluracursos.buensaborbp.repository.UsuarioRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

// Verifica el id_token que Google Identity Services le entregó al frontend, y resuelve (o
// crea) el Cliente correspondiente. Una vez resuelto el Usuario, AuthController genera el
// JWT propio exactamente igual que en el login normal — este servicio no toca esa parte.
@Service
public class GoogleAuthService {

    // Opcional a propósito (como mercadopago.access-token): si falta, no bloquea el arranque
    // de toda la app, solo hace fallar /login/google hasta que se configure.
    @Value("${google.oauth.client-id:}")
    private String googleClientId;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public GoogleAuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario autenticar(String idTokenString) {
        GoogleIdToken.Payload payload = verificar(idTokenString);
        String googleId = payload.getSubject();

        return usuarioRepository.findByOauth2Id(googleId)
                .or(() -> usuarioRepository.findByEmail(payload.getEmail())
                        .map(usuario -> vincularGoogle(usuario, googleId)))
                .orElseGet(() -> crearClienteDesdeGoogle(payload));
    }

    private GoogleIdToken.Payload verificar(String idTokenString) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new IllegalStateException("El login con Google no está configurado (falta google.oauth.client-id)");
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new BadCredentialsException("Token de Google inválido o vencido");
            }
            return idToken.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new BadCredentialsException("No se pudo verificar el token de Google", e);
        }
    }

    // Un usuario que ya se había registrado con email/contraseña y ahora entra por primera
    // vez con Google: se linkea por email en vez de crear una cuenta duplicada.
    private Usuario vincularGoogle(Usuario usuario, String googleId) {
        usuario.setOauth2Id(googleId);
        return usuarioRepository.save(usuario);
    }

    private Usuario crearClienteDesdeGoogle(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
        String nombre = (String) payload.get("given_name");
        String apellido = (String) payload.get("family_name");

        Cliente cliente = Cliente.builder()
                .nombre(nombre != null ? nombre : "Sin nombre")
                .apellido(apellido != null ? apellido : "Sin apellido")
                .usuario(generarNombreDeUsuario(email))
                .email(email)
                // Google no comparte teléfono/fecha de nacimiento en el login; quedan como
                // placeholder hasta que el cliente complete su perfil (no hay ese flujo
                // todavía — ver lista de pendientes).
                .telefono("Sin especificar")
                .fechaNacimiento(LocalDate.now().minusYears(18))
                // Nunca se usa para loguear (el login de esta cuenta es siempre por Google),
                // pero el campo es NOT NULL: se genera una contraseña aleatoria descartable.
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .rol(Rol.CLIENTE)
                .activo(true)
                .oauth2Id(payload.getSubject())
                .build();

        return usuarioRepository.save(cliente);
    }

    private String generarNombreDeUsuario(String email) {
        String base = email.substring(0, email.indexOf('@'));
        String candidato = base;
        int sufijo = 1;
        while (usuarioRepository.findByUsuario(candidato).isPresent()) {
            candidato = base + (sufijo++);
        }
        return candidato;
    }
}

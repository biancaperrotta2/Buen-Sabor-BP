package com.aluracursos.buensaborbp.infra.security;

import com.aluracursos.buensaborbp.entity.Usuario;
import com.aluracursos.buensaborbp.repository.UsuarioRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Verifica el idToken que el frontend obtiene de Firebase después de que el cliente
// inicia sesión (típicamente tras resetear la contraseña desde la ventana de Firebase),
// y resuelve el Usuario correspondiente en nuestra base. No crea clientes nuevos: solo
// tiene sentido para alguien que ya se registró acá (y por lo tanto ya tiene su cuenta
// espejo en Firebase, ver FirebaseUserMirrorService).
@Service
public class FirebaseLoginService {

    private final UsuarioRepository usuarioRepository;

    public FirebaseLoginService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Usuario autenticar(String idTokenString) {
        FirebaseToken decoded = verificar(idTokenString);

        return usuarioRepository.findByFirebaseUid(decoded.getUid())
                .or(() -> usuarioRepository.findByEmail(decoded.getEmail())
                        .map(usuario -> vincularFirebase(usuario, decoded.getUid())))
                .orElseThrow(() -> new BadCredentialsException(
                        "No existe una cuenta registrada con ese email"));
    }

    private FirebaseToken verificar(String idTokenString) {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idTokenString);
        } catch (FirebaseAuthException e) {
            throw new BadCredentialsException("Token de Firebase inválido o vencido", e);
        }
    }

    // Cubre el caso de una cuenta creada antes de que existiera el espejo en Firebase,
    // o si el espejo falló en su momento: se linkea por email en el primer login exitoso.
    private Usuario vincularFirebase(Usuario usuario, String firebaseUid) {
        usuario.setFirebaseUid(firebaseUid);
        return usuarioRepository.save(usuario);
    }
}

package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.FirebaseLoginRequest;
import com.aluracursos.buensaborbp.dto.GoogleLoginRequest;
import com.aluracursos.buensaborbp.dto.LoginRequest;
import com.aluracursos.buensaborbp.dto.LoginResponse;
import com.aluracursos.buensaborbp.entity.Usuario;
import com.aluracursos.buensaborbp.exception.CuentaDesactivadaException;
import com.aluracursos.buensaborbp.infra.security.FirebaseLoginService;
import com.aluracursos.buensaborbp.infra.security.GoogleAuthService;
import com.aluracursos.buensaborbp.infra.security.TokenService;
import com.aluracursos.buensaborbp.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;
    private final GoogleAuthService googleAuthService;
    private final FirebaseLoginService firebaseLoginService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService,
                           UsuarioRepository usuarioRepository, GoogleAuthService googleAuthService,
                           FirebaseLoginService firebaseLoginService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
        this.googleAuthService = googleAuthService;
        this.firebaseLoginService = firebaseLoginService;
    }

    @PostMapping
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // El login es por email (de cara afuera), pero Spring Security sigue autenticando
        // internamente contra Usuario.usuario (ver AutenticacionService/TokenService), así
        // que acá solo se resuelve uno a partir del otro; no cambia nada más del mecanismo
        // de JWT ya probado.
        String usuario = usuarioRepository.findByEmail(request.email())
                .map(Usuario::getUsuario)
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));

        var authToken = new UsernamePasswordAuthenticationToken(usuario, request.password());
        Authentication authentication = authenticationManager.authenticate(authToken);

        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        exigirActivo(usuarioAutenticado);
        String token = tokenService.generarToken(usuarioAutenticado);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    // El frontend obtiene el idToken con Google Identity Services (botón "Ingresar con
    // Google") y nos lo manda acá; no pasa por el AuthenticationManager porque no hay
    // contraseña propia involucrada.
    @PostMapping("/google")
    public ResponseEntity<LoginResponse> loginConGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        Usuario usuario = googleAuthService.autenticar(request.idToken());
        exigirActivo(usuario);
        String token = tokenService.generarToken(usuario);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    // El frontend obtiene este idToken de Firebase Authentication después de que el
    // cliente inicia sesión ahí (típicamente el paso siguiente a resetear la contraseña
    // desde "¿Olvidaste tu contraseña?", que abre la ventana propia de Firebase).
    @PostMapping("/firebase")
    public ResponseEntity<LoginResponse> loginConFirebase(@Valid @RequestBody FirebaseLoginRequest request) {
        Usuario usuario = firebaseLoginService.autenticar(request.idToken());
        exigirActivo(usuario);
        String token = tokenService.generarToken(usuario);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    private void exigirActivo(Usuario usuario) {
        if (!usuario.estaActivo()) {
            throw new CuentaDesactivadaException();
        }
    }
}

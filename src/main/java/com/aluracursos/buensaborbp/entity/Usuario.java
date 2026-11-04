package com.aluracursos.buensaborbp.entity;

import com.aluracursos.buensaborbp.entity.Enums.Rol;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_usuario;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String usuario; 

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String telefono;

    @Builder.Default
    @Column(name = "fecha_registro",nullable = false, updatable=false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Builder.Default
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImagenUsuario> imagenes = new ArrayList<>();

    // Para autenticación OAuth2 (Google, verificado directo con google-api-client)
    private String oauth2Id;

    // Id del usuario espejo en Firebase Authentication (usado solo para la
    // recuperación de contraseña: Firebase emite un idToken luego del reseteo,
    // y este campo permite resolver a qué Usuario de nuestra base corresponde).
    private String firebaseUid;

    // --- UserDetails (Spring Security) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getUsername() {
        return usuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Empleado y Cliente lo sobrescriben con su marca "activo". No se usa isEnabled() a propósito:
    // Spring lo evalúa antes de validar la contraseña y respondería distinto para una cuenta
    // desactivada, lo que permitiría descubrir cuentas sin conocer la clave.
    public boolean estaActivo() {
        return true;
    }
}

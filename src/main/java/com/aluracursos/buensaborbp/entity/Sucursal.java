package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Sucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_sucursal;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private LocalTime horarioApertura;

    @Column(nullable = false)
    private LocalTime horarioCierre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "domicilio_id", referencedColumnName = "id")
    private Domicilio domicilio;

    // Contempla el caso de un horario que cruza la medianoche (ej. 18:00 a 00:00).
    public boolean estaAbierta(LocalTime ahora) {
        if (horarioApertura.equals(horarioCierre)) {
            return true; // abierto las 24 horas
        }
        if (horarioApertura.isBefore(horarioCierre)) {
            return !ahora.isBefore(horarioApertura) && ahora.isBefore(horarioCierre);
        }
        return !ahora.isBefore(horarioApertura) || ahora.isBefore(horarioCierre);
    }

    // "Panamericana 1321, Godoy Cruz" (para "¿Cómo llegar?" y los diálogos del carrito).
    public String direccionTexto() {
        if (domicilio == null) {
            return null;
        }
        String ciudad = domicilio.getCiudad() != null ? ", " + domicilio.getCiudad().getNombre() : "";
        return domicilio.getCalle() + " " + domicilio.getNumero() + ciudad;
    }
}

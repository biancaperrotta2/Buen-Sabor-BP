package com.aluracursos.buensaborbp.dto;

import java.time.LocalTime;

// Datos públicos del local para el carrito: dirección ("¿Cómo llegar?") y horario ("Abre a las...").
public record LocalResponse(
    String nombre,
    String direccion,
    LocalTime horarioApertura,
    LocalTime horarioCierre,
    boolean abiertoAhora
) {}

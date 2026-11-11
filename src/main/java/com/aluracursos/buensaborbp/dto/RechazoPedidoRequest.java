package com.aluracursos.buensaborbp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// El cajero rechaza un pedido pendiente: el motivo es obligatorio porque es lo que ve el
// cliente en "Mis pedidos" (el aviso de cancelación que pide el enunciado).
public record RechazoPedidoRequest(
    @NotBlank(message = "Escribí el motivo del rechazo")
    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
    String motivo,

    // true cuando el reintegro automático falló y el cajero elige rechazar igual y devolver la
    // plata a mano desde el panel de Mercado Pago (no se vuelve a intentar contra la API).
    boolean reintegroManual
) {}

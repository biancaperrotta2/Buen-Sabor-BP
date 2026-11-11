package com.aluracursos.buensaborbp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// El cajero rechaza un pedido pendiente: el motivo es obligatorio porque es lo que ve el
// cliente en "Mis pedidos" (el aviso de cancelación que pide el enunciado).
public record RechazoPedidoRequest(
    @NotBlank(message = "Escribí el motivo del rechazo")
    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
    String motivo
) {}

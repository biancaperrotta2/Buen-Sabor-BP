package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.Estado;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoPedidoRequest(
    @NotNull(message = "El nuevo estado es obligatorio")
    Estado nuevoEstado
) {}

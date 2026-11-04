package com.aluracursos.buensaborbp.dto;

import java.time.LocalTime;
import java.util.List;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;
import com.aluracursos.buensaborbp.entity.Enums.Estado;

public record PedidoCocinaResponse(
    Long id,              
    ClienteBaseResponse cliente,       
    LocalTime horaEstimadaFinalizacion,
    TipoEnvio tipoEnvio,
    Estado estadoPedido,
    String aclaraciones,
    List<ItemCocinaResponse> items
) {}

package com.aluracursos.buensaborbp.dto;

import java.util.List;

import com.aluracursos.buensaborbp.entity.Enums.FormaPago;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;

public record PedidoRequest(
    TipoEnvio tipoEnvio,
    FormaPago formaPago,
    Long idDomicilio,
    String aclaracionDomicilio,
    List<DetallePedidoRequest> detallePedidos
) { }

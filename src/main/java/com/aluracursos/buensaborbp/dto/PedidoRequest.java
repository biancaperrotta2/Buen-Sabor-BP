package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.FormaPago;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;
import jakarta.validation.constraints.NotNull;

// El pedido ya no recibe el detalle de artículos en el body: se arma a partir del
// carrito persistido del cliente autenticado (ver CarritoController / Carrito).
public record PedidoRequest(
    @NotNull(message = "El tipo de envío es obligatorio")
    TipoEnvio tipoEnvio,

    @NotNull(message = "La forma de pago es obligatoria")
    FormaPago formaPago,

    @NotNull(message = "El domicilio es obligatorio")
    Long idDomicilio,

    String aclaracionDomicilio
) { }

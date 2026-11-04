package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.FormaPago;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// El pedido ya no recibe el detalle de artículos en el body: se arma a partir del
// carrito persistido del cliente autenticado (ver CarritoController / Carrito).
// idDomicilio solo es obligatorio para DELIVERY (validado en PedidoServiceImpl,
// no acá con @NotNull) — un retiro en el local no necesita una dirección.
public record PedidoRequest(
    @NotNull(message = "El tipo de envío es obligatorio")
    TipoEnvio tipoEnvio,

    @NotNull(message = "La forma de pago es obligatoria")
    FormaPago formaPago,

    Long idDomicilio,

    String aclaracionDomicilio,

    @Size(max = 255, message = "Las aclaraciones no pueden superar los 255 caracteres")
    String aclaraciones
) { }

package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import com.aluracursos.buensaborbp.dto.PromocionDetalleRequest;
import com.aluracursos.buensaborbp.entity.Enums.TipoPromocion;

public record PromocionRequest(
    String nombre,
    String descripcionDescuento,
    LocalDate fechaDesde,
    LocalDate fechaHasta,
    LocalTime horaDesde,
    LocalTime horaHasta,
    BigDecimal precioVenta, 
    TipoPromocion tipoPromocion,
    Long idCategoria,          
    List<PromocionDetalleRequest> detalles, // DTO para enviar ID Articulo y Cantidad
    List<String> imagenesUrls   
) {}


package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.aluracursos.buensaborbp.dto.ImagenPromocionResponse;
import com.aluracursos.buensaborbp.dto.PromocionDetalleResponse;
import com.aluracursos.buensaborbp.entity.Enums.TipoPromocion;

public record PromocionFullResponse(
    Long id,
    String nombre,
    String descripcionDescuento,
    LocalDate fechaDesde,
    LocalDate fechaHasta,
    LocalTime horaDesde,
    LocalTime horaHasta,
    BigDecimal precioTotal, 
    BigDecimal precioVenta, 
    BigDecimal precioCosto, 
    boolean activo,
    TipoPromocion tipoPromocion, 
    String nombreCategoria,     
    List<PromocionDetalleResponse> detalles,
    List<ImagenPromocionResponse> imagenes 
) {}

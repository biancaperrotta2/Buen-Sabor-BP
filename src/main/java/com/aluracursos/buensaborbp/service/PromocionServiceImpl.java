package com.aluracursos.buensaborbp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aluracursos.buensaborbp.dto.PromocionRequest;
import com.aluracursos.buensaborbp.entity.Promocion;
import com.aluracursos.buensaborbp.entity.Articulo;
import com.aluracursos.buensaborbp.entity.PromocionDetalle;
import com.aluracursos.buensaborbp.repository.PromocionRepository;
import com.aluracursos.buensaborbp.repository.ArticuloRepository;
import com.aluracursos.buensaborbp.mapper.PromocionMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import com.aluracursos.buensaborbp.dto.PromocionBaseResponse;
import com.aluracursos.buensaborbp.dto.PromocionFullResponse;
import java.util.ArrayList;

@Service
public class PromocionServiceImpl extends BaseServiceImpl<Promocion, Long> implements PromocionService {

    private final PromocionRepository promocionRepository;
    private final PromocionMapper promocionMapper;
    private final ArticuloRepository articuloRepository;

    public PromocionServiceImpl(PromocionRepository promocionRepository, 
                                PromocionMapper promocionMapper, 
                                ArticuloRepository articuloRepository) {
        super(promocionRepository);
        this.promocionRepository = promocionRepository;
        this.promocionMapper = promocionMapper;
        this.articuloRepository = articuloRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionBaseResponse> listarParaMenu() {
        return promocionMapper.toBaseResponseList(promocionRepository.findVigentes());
    }

    @Override
    @Transactional(readOnly = true)
    public PromocionFullResponse buscarPorIdFull(Long id) {
        Promocion p = promocionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Promoción no encontrada"));
        return promocionMapper.toFullResponse(p);
    }

    @Override
    @Transactional
    public PromocionFullResponse crearPromocion(PromocionRequest request) {
        Promocion p = Promocion.builder()
            .nombre(request.nombre())
            .fechaDesde(request.fechaDesde())
            .fechaHasta(request.fechaHasta())
            .horaDesde(request.horaDesde())
            .horaHasta(request.horaHasta())
            .descripcionDescuento(request.descripcionDescuento())
            .promocionDetalle(new ArrayList<>())
            .build();

        // Agregamos los artículos al detalle
        request.detalles().forEach(d -> {
            Articulo art = articuloRepository.findById(d.idArticulo()).get();
            p.getPromocionDetalle().add(PromocionDetalle.builder()
                .articulos(art)
                .cantidad(d.cantidad())
                .articuloPromocion(p)
                .build());
        });

        // Aplicamos métodos de cálculo de la entidad
        p.calcularPrecioCosto();
        p.calcularPrecioTotal();
        p.calcularPrecioVenta(request.precioVenta()); 

        return promocionMapper.toFullResponse(promocionRepository.save(p));
    }

    @Override
    @Transactional
    public PromocionFullResponse actualizarPromocion(Long id, PromocionRequest request) {
        // Buscar la promoción existente
        Promocion promocion = promocionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Promoción no encontrada"));

        // Actualizar campos principales
        promocion.setNombre(request.nombre());
        promocion.setDescripcionDescuento(request.descripcionDescuento());
        promocion.setFechaDesde(request.fechaDesde());
        promocion.setFechaHasta(request.fechaHasta());
        promocion.setHoraDesde(request.horaDesde());
        promocion.setHoraHasta(request.horaHasta());

        // Actualizar detalles: limpiar y agregar los nuevos detalles
        promocion.getPromocionDetalle().clear();
        if (request.detalles() != null) {
            request.detalles().forEach(d -> {
                Articulo art = articuloRepository.findById(d.idArticulo()).get();
                promocion.getPromocionDetalle().add(PromocionDetalle.builder()
                    .articulos(art)
                    .cantidad(d.cantidad())
                    .articuloPromocion(promocion)
                    .build());
            });
        }

        // Aplicar lógica de recalculo de precios en la entidad
        promocion.calcularPrecioCosto();
        promocion.calcularPrecioTotal();
        promocion.calcularPrecioVenta(request.precioVenta());

        // Guardar cambios y devolver el dto
        promocionRepository.save(promocion);

        return promocionMapper.toFullResponse(promocion);
    }

@Override
@Transactional(readOnly = true)
public List<PromocionFullResponse> listarTodoParaAdmin() {
    // Obtener todas las promociones (habilitadas y no habilitadas)
    List<Promocion> promociones = promocionRepository.findAll();

    // Mapear cada promoción a su DTO correspondiente
    return promociones.stream()
            .map(promocionMapper::toFullResponse)
            .toList();
}

@Override
@Transactional
public void cambiarEstadoHabilitacion(Long id) {
    // Buscar la promoción por su id
    Promocion promocion = promocionRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Promoción no encontrada con id: " + id));

    // Cambiar el estado de habilitación (baja lógica)
    promocion.setActivo(!Boolean.TRUE.equals(promocion.getActivo()));

    // Guardar cambios
    promocionRepository.save(promocion);
}

}

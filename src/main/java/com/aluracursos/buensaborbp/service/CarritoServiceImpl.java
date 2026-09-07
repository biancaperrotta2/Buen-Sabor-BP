package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.AgregarItemCarritoRequest;
import com.aluracursos.buensaborbp.dto.CarritoResponse;
import com.aluracursos.buensaborbp.entity.Articulo;
import com.aluracursos.buensaborbp.entity.Carrito;
import com.aluracursos.buensaborbp.entity.Cliente;
import com.aluracursos.buensaborbp.entity.DetalleCarrito;
import com.aluracursos.buensaborbp.mapper.CarritoMapper;
import com.aluracursos.buensaborbp.repository.ArticuloRepository;
import com.aluracursos.buensaborbp.repository.CarritoRepository;
import com.aluracursos.buensaborbp.repository.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final ClienteRepository clienteRepository;
    private final ArticuloRepository articuloRepository;
    private final CarritoMapper carritoMapper;

    public CarritoServiceImpl(CarritoRepository carritoRepository,
                               ClienteRepository clienteRepository,
                               ArticuloRepository articuloRepository,
                               CarritoMapper carritoMapper) {
        this.carritoRepository = carritoRepository;
        this.clienteRepository = clienteRepository;
        this.articuloRepository = articuloRepository;
        this.carritoMapper = carritoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CarritoResponse obtenerMiCarrito(Long clienteId) {
        return carritoRepository.findByClienteId(clienteId)
                .map(carritoMapper::toResponse)
                .orElseGet(() -> new CarritoResponse(List.of(), BigDecimal.ZERO));
    }

    @Override
    @Transactional
    public CarritoResponse agregarItem(Long clienteId, AgregarItemCarritoRequest request) {
        Articulo articulo = articuloRepository.findById(request.idArticulo())
                .orElseThrow(() -> new EntityNotFoundException("Artículo no encontrado"));

        // Nota: no se valida articulo.getEsVendible() acá porque ningún mapper/service de
        // ArticuloManufacturado lo setea al crear (solo ArticuloInsumoMapper lo toca, y para
        // insumos) — en la data real queda null aun en platos vendibles. "activo" es el único
        // criterio de disponibilidad que el resto de la app usa de forma consistente (getActivos()).
        if (!Boolean.TRUE.equals(articulo.getActivo())) {
            throw new IllegalStateException("El artículo no está disponible para la venta");
        }

        Carrito carrito = carritoRepository.findByClienteId(clienteId)
                .orElseGet(() -> {
                    Cliente cliente = clienteRepository.findById(clienteId)
                            .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
                    return Carrito.builder().cliente(cliente).items(new ArrayList<>()).build();
                });

        DetalleCarrito existente = carrito.getItems().stream()
                .filter(item -> item.getArticulo().getId().equals(articulo.getId()))
                .findFirst()
                .orElse(null);

        if (existente != null) {
            existente.setCantidad(existente.getCantidad() + request.cantidad());
        } else {
            carrito.getItems().add(DetalleCarrito.builder()
                    .articulo(articulo)
                    .cantidad(request.cantidad())
                    .carrito(carrito)
                    .build());
        }

        return carritoMapper.toResponse(carritoRepository.save(carrito));
    }

    @Override
    @Transactional
    public CarritoResponse actualizarCantidad(Long clienteId, Long idArticulo, int cantidad) {
        Carrito carrito = carritoRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("El carrito está vacío"));

        DetalleCarrito item = carrito.getItems().stream()
                .filter(i -> i.getArticulo().getId().equals(idArticulo))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("El artículo no está en el carrito"));

        item.setCantidad(cantidad);

        return carritoMapper.toResponse(carritoRepository.save(carrito));
    }

    @Override
    @Transactional
    public CarritoResponse eliminarItem(Long clienteId, Long idArticulo) {
        Carrito carrito = carritoRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("El carrito está vacío"));

        boolean removido = carrito.getItems().removeIf(i -> i.getArticulo().getId().equals(idArticulo));
        if (!removido) {
            throw new EntityNotFoundException("El artículo no está en el carrito");
        }

        return carritoMapper.toResponse(carritoRepository.save(carrito));
    }

    @Override
    @Transactional
    public void vaciarCarrito(Long clienteId) {
        carritoRepository.findByClienteId(clienteId).ifPresent(carrito -> {
            carrito.getItems().clear();
            carritoRepository.save(carrito);
        });
    }
}

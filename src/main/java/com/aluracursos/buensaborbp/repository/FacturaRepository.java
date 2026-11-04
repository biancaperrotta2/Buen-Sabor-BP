package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Factura;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;

@Repository
public interface FacturaRepository extends BaseRepository<Factura, Long> {
    Optional<Factura> findByPedidoId(Long pedidoId);
    boolean existsByPedidoId(Long pedidoId);
    List<Factura> findByFechaFacturacionBetween(LocalDate inicio, LocalDate fin);

    // Tabla de admin "Facturas": búsqueda libre por número de comprobante o nombre/apellido
    // del cliente del pedido facturado.
    @Query("SELECT f FROM Factura f WHERE :busqueda = '' " +
            "OR LOWER(CAST(f.numeroComprobante AS string)) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(f.pedido.cliente.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(f.pedido.cliente.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Factura> buscar(@Param("busqueda") String busqueda, Pageable pageable);
}


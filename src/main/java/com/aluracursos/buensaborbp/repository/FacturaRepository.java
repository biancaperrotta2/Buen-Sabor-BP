package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Factura;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;

@Repository
public interface FacturaRepository extends BaseRepository<Factura, Long> {
    Optional<Factura> findByPedidoId(Long pedidoId);
    boolean existsByPedidoId(Long pedidoId);
    List<Factura> findByFechaFacturacionBetween(LocalDate inicio, LocalDate fin);
}


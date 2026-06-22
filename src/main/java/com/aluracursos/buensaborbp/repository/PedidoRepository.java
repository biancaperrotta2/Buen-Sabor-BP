package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Pedido;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends BaseRepository<Pedido, Long> {
    
    // Cambiamos List<Estado> por List<EstadoPedido>
    List<Pedido> findByEstadoPedidoInOrderByHoraEstimadaFinalizacionAsc(List<Estado> estados);

    // Historial para el cliente (se mantiene igual, asumiendo que el ID es Long)
    List<Pedido> findByClienteIdOrderByFechaPedidoDesc(Long clienteId);
}
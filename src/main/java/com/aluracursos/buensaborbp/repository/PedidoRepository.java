package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    /**
     * Busca todos los pedidos de un cliente específico.
     * 
     * @param clienteId ID del cliente
     * @return Lista de pedidos del cliente
     */
    List<Pedido> findByClienteId(Long clienteId);
    
    /**
     * Busca todos los pedidos con un estado específico.
     * 
     * @param estado Estado del pedido
     * @return Lista de pedidos con el estado especificado
     */
    List<Pedido> findByEstadoPedido(Estado estado);
    
    /**
     * Busca todos los pedidos con un estado de pago específico.
     * 
     * @param estadoPago Estado de pago
     * @return Lista de pedidos con el estado de pago especificado
     */
    List<Pedido> findByEstadoPago(Estado estadoPago);
}

package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;
import com.aluracursos.buensaborbp.entity.Pedido;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PedidoRepository extends BaseRepository<Pedido, Long> {

    List<Pedido> findByEstadoInOrderByHoraEstimadaFinalizacionAsc(List<Estado> estados);

    // El PK de Cliente/Empleado (heredado de Usuario) es id_usuario, no id: no se puede derivar por nombre.
    @Query("SELECT p FROM Pedido p WHERE p.cliente.id_usuario = :clienteId ORDER BY p.fechaPedido DESC")
    List<Pedido> findByClienteIdOrderByFechaPedidoDesc(@Param("clienteId") Long clienteId);

    // Pool de pedidos listos para salir a repartir, todavía sin un repartidor asignado.
    @Query("SELECT p FROM Pedido p WHERE p.estado = :estado AND p.tipoEnvio = :tipoEnvio AND p.repartidor IS NULL ORDER BY p.horaEstimadaFinalizacion ASC")
    List<Pedido> findDisponiblesParaEnvio(@Param("estado") Estado estado, @Param("tipoEnvio") TipoEnvio tipoEnvio);

    @Query("SELECT p FROM Pedido p WHERE p.repartidor.id_usuario = :repartidorId ORDER BY p.fechaPedido DESC")
    List<Pedido> findByRepartidorIdOrderByFechaPedidoDesc(@Param("repartidorId") Long repartidorId);

    // Pedidos listos para retirar en el mostrador (a diferencia de disponibles-envio, que es delivery).
    List<Pedido> findByEstadoAndTipoEnvioOrderByHoraEstimadaFinalizacionAsc(Estado estado, TipoEnvio tipoEnvio);

    // --- Reportes de administrador ---
    List<Pedido> findByFechaPedidoBetweenAndEstado(LocalDate desde, LocalDate hasta, Estado estado);

    List<Pedido> findByFechaPedidoBetweenAndEstadoNot(LocalDate desde, LocalDate hasta, Estado estadoExcluido);
}
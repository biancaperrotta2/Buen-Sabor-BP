package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;
import com.aluracursos.buensaborbp.entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Pedidos rechazados cuyo pago de Mercado Pago el cajero todavía tiene que devolver a mano.
    List<Pedido> findByReintegroPendienteTrueOrderByFechaPedidoAsc();

    // Tabla de Órdenes (cajero): filtro opcional por estado + búsqueda por número o cliente.
    @Query("SELECT p FROM Pedido p LEFT JOIN p.cliente c WHERE (:estado IS NULL OR p.estado = :estado) AND (:busqueda = '' " +
            "OR CAST(p.id AS string) LIKE CONCAT('%', :busqueda, '%') " +
            "OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    Page<Pedido> buscarParaOrdenes(@Param("estado") Estado estado, @Param("busqueda") String busqueda, Pageable pageable);

    // Tabla de Cocina: solo esos estados + la misma búsqueda.
    @Query("SELECT p FROM Pedido p LEFT JOIN p.cliente c WHERE p.estado IN :estados AND (:busqueda = '' " +
            "OR CAST(p.id AS string) LIKE CONCAT('%', :busqueda, '%') " +
            "OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    Page<Pedido> buscarEnEstados(@Param("estados") List<Estado> estados, @Param("busqueda") String busqueda, Pageable pageable);

    // --- Reportes de administrador ---
    List<Pedido> findByFechaPedidoBetweenAndEstado(LocalDate desde, LocalDate hasta, Estado estado);

    List<Pedido> findByFechaPedidoBetweenAndEstadoNot(LocalDate desde, LocalDate hasta, Estado estadoExcluido);
}
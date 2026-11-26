package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ExistenciaInsumoRequest;
import com.aluracursos.buensaborbp.dto.ExistenciaInsumoResponse;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.ExistenciaInsumo;
import com.aluracursos.buensaborbp.repository.ArticuloInsumoRepository;
import com.aluracursos.buensaborbp.repository.ExistenciaInsumoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

// Compras ("existencias") de un insumo, registro simple elegido por la usuaria:
// - cargar una compra suma su cantidad al stock actual;
// - corregirla ajusta la diferencia; borrarla la descuenta;
// - no se permite dejar el stock por debajo de lo reservado por pedidos en curso (ni negativo);
// - el precio de costo del insumo pasa a ser el de la compra más reciente (y con él se
//   recalculan su precio de venta, si tiene margen, y el costo de los productos que lo usan).
@Service
public class ExistenciaInsumoService {

    private final ArticuloInsumoRepository insumoRepository;
    private final ExistenciaInsumoRepository existenciaRepository;
    private final RecalculoCostoProductos recalculoCostoProductos;

    public ExistenciaInsumoService(ArticuloInsumoRepository insumoRepository,
                                   ExistenciaInsumoRepository existenciaRepository,
                                   RecalculoCostoProductos recalculoCostoProductos) {
        this.insumoRepository = insumoRepository;
        this.existenciaRepository = existenciaRepository;
        this.recalculoCostoProductos = recalculoCostoProductos;
    }

    @Transactional(readOnly = true)
    public List<ExistenciaInsumoResponse> listar(Long insumoId) {
        ArticuloInsumo insumo = buscarInsumo(insumoId);
        LocalDate hoy = LocalDate.now();
        return existenciaRepository.findByInsumo_IdOrderByFechaCompraDescIdDesc(insumoId).stream()
                .map(e -> aResponse(insumo, e, hoy))
                .toList();
    }

    @Transactional
    public ExistenciaInsumoResponse crear(Long insumoId, ExistenciaInsumoRequest request) {
        ArticuloInsumo insumo = buscarInsumo(insumoId);
        LocalDate fechaCompra = validarFechas(request);
        double cantidadBase = insumo.convertirACantidadBase(request.cantidad());

        ajustarStock(insumo, cantidadBase, "cargar");
        ExistenciaInsumo compra = existenciaRepository.save(ExistenciaInsumo.builder()
                .insumo(insumo)
                .fechaCompra(fechaCompra)
                .fechaVencimiento(request.fechaVencimiento())
                .cantidad(cantidadBase)
                .costoUnitario(request.costoUnitario())
                .build());
        existenciaRepository.flush();
        actualizarCosto(insumo);
        return aResponse(insumo, compra, LocalDate.now());
    }

    @Transactional
    public ExistenciaInsumoResponse editar(Long existenciaId, ExistenciaInsumoRequest request) {
        ExistenciaInsumo compra = buscarExistencia(existenciaId);
        ArticuloInsumo insumo = compra.getInsumo();
        LocalDate fechaCompra = validarFechas(request);
        double cantidadBase = insumo.convertirACantidadBase(request.cantidad());

        ajustarStock(insumo, cantidadBase - compra.getCantidad(), "corregir");
        compra.setFechaCompra(fechaCompra);
        compra.setFechaVencimiento(request.fechaVencimiento());
        compra.setCantidad(cantidadBase);
        compra.setCostoUnitario(request.costoUnitario());
        existenciaRepository.save(compra);
        existenciaRepository.flush();
        actualizarCosto(insumo);
        return aResponse(insumo, compra, LocalDate.now());
    }

    @Transactional
    public void eliminar(Long existenciaId) {
        ExistenciaInsumo compra = buscarExistencia(existenciaId);
        ArticuloInsumo insumo = compra.getInsumo();
        ajustarStock(insumo, -compra.getCantidad(), "borrar");
        existenciaRepository.delete(compra);
        existenciaRepository.flush();
        actualizarCosto(insumo);
    }

    // ---------- auxiliares ----------

    private LocalDate validarFechas(ExistenciaInsumoRequest request) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaCompra = request.fechaCompra() != null ? request.fechaCompra() : hoy;
        if (fechaCompra.isAfter(hoy)) {
            throw new IllegalArgumentException("La fecha de compra no puede ser futura");
        }
        if (request.fechaVencimiento().isBefore(fechaCompra)) {
            throw new IllegalArgumentException("El vencimiento no puede ser anterior a la fecha de compra");
        }
        return fechaCompra;
    }

    // Suma (o resta) la diferencia al stock actual sin dejarlo por debajo de lo reservado.
    private void ajustarStock(ArticuloInsumo insumo, double diferenciaBase, String accion) {
        double nuevo = redondear(insumo.getStockActual() + diferenciaBase);
        double reservado = insumo.getStockReservado() == null ? 0 : insumo.getStockReservado();
        if (nuevo < reservado - 1e-9) {
            String unidad = insumo.getUnidadMedidaEnum().getAbreviatura();
            String motivo = reservado > 0
                    ? " y hay " + formato(insumo.convertirDesdeCantidadBase(reservado)) + " " + unidad + " reservados por pedidos en curso."
                    : ": parte de esa compra ya se usó.";
            throw new IllegalStateException("No se puede " + accion + " esta compra: el stock de \"" + insumo.getNombre()
                    + "\" quedaría en " + formato(insumo.convertirDesdeCantidadBase(nuevo)) + " " + unidad + motivo);
        }
        insumo.setStockActual(nuevo);
    }

    // Precio de costo = el de la compra más reciente (si no quedan compras, no se toca). Si
    // cambia, se recalcula el precio de venta del insumo (cuando tiene margen) y el costo de los
    // productos que lo usan.
    private void actualizarCosto(ArticuloInsumo insumo) {
        boolean cambioCosto = existenciaRepository.findFirstByInsumo_IdOrderByFechaCompraDescIdDesc(insumo.getId())
                .map(ExistenciaInsumo::getCostoUnitario)
                .filter(costo -> insumo.getPrecioCosto() == null || insumo.getPrecioCosto().compareTo(costo) != 0)
                .map(costo -> {
                    insumo.setPrecioCosto(costo);
                    if (insumo.getPrecioVenta() != null) {
                        insumo.calcularPrecioVenta(insumo.getPrecioVenta());
                    }
                    return true;
                })
                .orElse(false);
        insumoRepository.save(insumo);
        if (cambioCosto) {
            recalculoCostoProductos.recalcularProductosQueUsan(insumo.getId());
        }
    }

    private ExistenciaInsumoResponse aResponse(ArticuloInsumo insumo, ExistenciaInsumo e, LocalDate hoy) {
        BigDecimal total = e.getCostoUnitario().multiply(BigDecimal.valueOf(e.getCantidad())).setScale(2, RoundingMode.HALF_UP);
        return new ExistenciaInsumoResponse(e.getId(), e.getFechaCompra(), insumo.convertirDesdeCantidadBase(e.getCantidad()),
                e.getCostoUnitario(), total, e.getFechaVencimiento(), e.getFechaVencimiento().isBefore(hoy));
    }

    private ArticuloInsumo buscarInsumo(Long id) {
        return insumoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el insumo con ID: " + id));
    }

    private ExistenciaInsumo buscarExistencia(Long id) {
        return existenciaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la existencia con ID: " + id));
    }

    private static double redondear(double valor) {
        return Math.round(valor * 1_000_000d) / 1_000_000d;
    }

    private static String formato(double valor) {
        return BigDecimal.valueOf(valor).stripTrailingZeros().toPlainString();
    }
}

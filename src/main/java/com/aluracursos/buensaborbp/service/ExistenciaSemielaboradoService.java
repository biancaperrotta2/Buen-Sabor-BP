package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ExistenciaSemielaboradoRequest;
import com.aluracursos.buensaborbp.dto.ExistenciaSemielaboradoResponse;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturadoDetalle;
import com.aluracursos.buensaborbp.entity.ExistenciaSemielaborado;
import com.aluracursos.buensaborbp.repository.ArticuloManufacturadoRepository;
import com.aluracursos.buensaborbp.repository.ExistenciaSemielaboradoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Lotes ("existencias") de un semielaborado. Preparar un lote descuenta de los insumos lo que
// pide la receta (escrita para 1 unidad del semielaborado); corregir o borrar un lote que todavía
// no se usó devuelve la diferencia. Un lote vencido no cuenta como stock y se puede dar de baja
// (se tiró: no devuelve insumos).
@Service
public class ExistenciaSemielaboradoService {

    private final ArticuloManufacturadoRepository manufacturadoRepository;
    private final ExistenciaSemielaboradoRepository existenciaRepository;

    public ExistenciaSemielaboradoService(ArticuloManufacturadoRepository manufacturadoRepository,
                                          ExistenciaSemielaboradoRepository existenciaRepository) {
        this.manufacturadoRepository = manufacturadoRepository;
        this.existenciaRepository = existenciaRepository;
    }

    @Transactional
    public List<ExistenciaSemielaboradoResponse> listar(Long semielaboradoId) {
        ArticuloManufacturado semi = buscarSemielaborado(semielaboradoId);
        LocalDate hoy = LocalDate.now();
        return semi.getExistencias().stream()
                .sorted(Comparator.comparing(ExistenciaSemielaborado::getFechaVencimiento)
                        .thenComparing(ExistenciaSemielaborado::getFechaElaboracion))
                .map(e -> aResponse(e, hoy))
                .toList();
    }

    @Transactional
    public ExistenciaSemielaboradoResponse crear(Long semielaboradoId, ExistenciaSemielaboradoRequest request) {
        ArticuloManufacturado semi = buscarSemielaborado(semielaboradoId);
        LocalDate hoy = LocalDate.now();
        LocalDate elaboracion = request.fechaElaboracion() != null ? request.fechaElaboracion() : hoy;
        if (elaboracion.isAfter(hoy)) {
            throw new IllegalArgumentException("La fecha de elaboración no puede ser futura");
        }
        validarVencimiento(elaboracion, request.fechaVencimiento());
        if (semi.getArticuloManufacturadoDetalles().isEmpty()) {
            throw new IllegalStateException("\"" + semi.getNombre() + "\" no tiene receta cargada: no se sabe qué insumos descontar");
        }

        descontarInsumos(semi, request.cantidad());
        semi.calcularPrecioCosto();

        ExistenciaSemielaborado lote = ExistenciaSemielaborado.builder()
                .semielaborado(semi)
                .fechaElaboracion(elaboracion)
                .fechaVencimiento(request.fechaVencimiento())
                .cantidad(request.cantidad())
                .cantidadRestante(request.cantidad())
                .costoUnitario(semi.getPrecioCosto())
                .build();
        semi.getExistencias().add(lote);
        semi.actualizarStockDesdeExistencias(hoy);
        manufacturadoRepository.save(semi);
        return aResponse(lote, hoy);
    }

    // Solo cantidad y vencimiento; la diferencia de cantidad se descuenta o se devuelve a los insumos.
    @Transactional
    public ExistenciaSemielaboradoResponse editar(Long existenciaId, ExistenciaSemielaboradoRequest request) {
        ExistenciaSemielaborado lote = buscarLoteModificable(existenciaId);
        ArticuloManufacturado semi = lote.getSemielaborado();
        LocalDate hoy = LocalDate.now();
        validarVencimiento(lote.getFechaElaboracion(), request.fechaVencimiento());

        double diferencia = request.cantidad() - lote.getCantidad();
        if (diferencia > 0) {
            descontarInsumos(semi, diferencia);
        } else if (diferencia < 0) {
            exigirQueNoAfecteReservas(semi, lote, request.cantidad(), hoy);
            devolverInsumos(semi, -diferencia);
        }
        lote.setCantidad(request.cantidad());
        lote.setCantidadRestante(request.cantidad());
        lote.setFechaVencimiento(request.fechaVencimiento());
        semi.actualizarStockDesdeExistencias(hoy);
        manufacturadoRepository.save(semi);
        return aResponse(lote, hoy);
    }

    @Transactional
    public void eliminar(Long existenciaId) {
        ExistenciaSemielaborado lote = buscarLoteModificable(existenciaId);
        ArticuloManufacturado semi = lote.getSemielaborado();
        LocalDate hoy = LocalDate.now();
        exigirQueNoAfecteReservas(semi, lote, 0, hoy);
        devolverInsumos(semi, lote.getCantidad());
        semi.getExistencias().remove(lote);
        semi.actualizarStockDesdeExistencias(hoy);
        manufacturadoRepository.save(semi);
    }

    // Lote vencido que se tiró: deja de contar y no devuelve insumos.
    @Transactional
    public ExistenciaSemielaboradoResponse descartar(Long existenciaId) {
        ExistenciaSemielaborado lote = existenciaRepository.findById(existenciaId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el lote " + existenciaId));
        LocalDate hoy = LocalDate.now();
        if (Boolean.TRUE.equals(lote.getDescartado())) {
            throw new IllegalStateException("El lote ya fue dado de baja");
        }
        if (!lote.estaVencido(hoy)) {
            throw new IllegalStateException("Solo se puede dar de baja un lote vencido (este vence el "
                    + lote.getFechaVencimiento() + ")");
        }
        lote.setDescartado(true);
        ArticuloManufacturado semi = lote.getSemielaborado();
        semi.actualizarStockDesdeExistencias(hoy);
        manufacturadoRepository.save(semi);
        return aResponse(lote, hoy);
    }

    private ArticuloManufacturado buscarSemielaborado(Long id) {
        ArticuloManufacturado semi = manufacturadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el semielaborado " + id));
        if (!Boolean.TRUE.equals(semi.getEsSemielaborado())) {
            throw new IllegalArgumentException("\"" + semi.getNombre() + "\" no es un semielaborado");
        }
        return semi;
    }

    private ExistenciaSemielaborado buscarLoteModificable(Long existenciaId) {
        ExistenciaSemielaborado lote = existenciaRepository.findById(existenciaId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el lote " + existenciaId));
        if (Boolean.TRUE.equals(lote.getDescartado())) {
            throw new IllegalStateException("El lote fue dado de baja y ya no se puede modificar");
        }
        if (!lote.sinUsar()) {
            throw new IllegalStateException("El lote ya se usó en pedidos: no se puede corregir ni borrar");
        }
        return lote;
    }

    private void validarVencimiento(LocalDate elaboracion, LocalDate vencimiento) {
        if (vencimiento.isBefore(elaboracion)) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la de elaboración");
        }
    }

    // Achicar o borrar un lote no puede dejar menos stock vigente que lo ya reservado por pedidos.
    private void exigirQueNoAfecteReservas(ArticuloManufacturado semi, ExistenciaSemielaborado lote,
                                           double cantidadNueva, LocalDate hoy) {
        if (!lote.estaVigente(hoy)) return;
        semi.actualizarStockDesdeExistencias(hoy);
        double vigenteDespues = semi.getStockActual() - lote.getCantidadRestante() + cantidadNueva;
        double reservado = semi.getStockReservado() != null ? semi.getStockReservado() : 0.0;
        if (vigenteDespues < reservado) {
            throw new IllegalStateException("No se puede: hay " + reservado + " de \"" + semi.getNombre()
                    + "\" reservados por pedidos y quedarían solo " + vigenteDespues);
        }
    }

    // Descuenta de cada insumo la receta × cantidad. Primero verifica todos, para no dejar el
    // descuento a medias si falta alguno.
    private void descontarInsumos(ArticuloManufacturado semi, double cantidad) {
        List<String> faltantes = new ArrayList<>();
        for (ArticuloManufacturadoDetalle detalle : semi.getArticuloManufacturadoDetalles()) {
            ArticuloInsumo insumo = insumoDe(detalle);
            double necesario = insumo.convertirACantidadBase(detalle.getCantidad() * cantidad);
            double disponible = insumo.getStockDisponible();
            if (disponible + 1e-9 < necesario) {
                faltantes.add(insumo.getNombre() + " (hacen falta " + formatear(insumo.convertirDesdeCantidadBase(necesario))
                        + " " + insumo.getUnidadMedidaEnum().getAbreviatura() + ", hay "
                        + formatear(insumo.convertirDesdeCantidadBase(disponible)) + ")");
            }
        }
        if (!faltantes.isEmpty()) {
            throw new IllegalStateException("No alcanzan los insumos para preparar " + formatear(cantidad) + " "
                    + semi.getUnidadMedidaEnum().getAbreviatura() + " de \"" + semi.getNombre() + "\": "
                    + String.join(", ", faltantes));
        }
        for (ArticuloManufacturadoDetalle detalle : semi.getArticuloManufacturadoDetalles()) {
            ArticuloInsumo insumo = insumoDe(detalle);
            double necesario = insumo.convertirACantidadBase(detalle.getCantidad() * cantidad);
            insumo.setStockActual(redondear(insumo.getStockActual() - necesario));
        }
    }

    private void devolverInsumos(ArticuloManufacturado semi, double cantidad) {
        for (ArticuloManufacturadoDetalle detalle : semi.getArticuloManufacturadoDetalles()) {
            ArticuloInsumo insumo = insumoDe(detalle);
            double devuelto = insumo.convertirACantidadBase(detalle.getCantidad() * cantidad);
            insumo.setStockActual(redondear(insumo.getStockActual() + devuelto));
        }
    }

    private ArticuloInsumo insumoDe(ArticuloManufacturadoDetalle detalle) {
        if (Hibernate.unproxy(detalle.getComponente()) instanceof ArticuloInsumo insumo) {
            return insumo;
        }
        throw new IllegalStateException("La receta tiene un ingrediente que no es un insumo: "
                + detalle.getComponente().getNombre());
    }

    private ExistenciaSemielaboradoResponse aResponse(ExistenciaSemielaborado e, LocalDate hoy) {
        String estado;
        if (Boolean.TRUE.equals(e.getDescartado())) estado = "DESCARTADO";
        else if (e.estaVencido(hoy)) estado = "VENCIDO";
        else if (e.getCantidadRestante() <= 0) estado = "AGOTADO";
        else estado = "VIGENTE";
        BigDecimal costoTotal = e.getCostoUnitario() == null ? null
                : e.getCostoUnitario().multiply(BigDecimal.valueOf(e.getCantidad())).setScale(2, RoundingMode.HALF_UP);
        boolean editable = !Boolean.TRUE.equals(e.getDescartado()) && e.sinUsar();
        return new ExistenciaSemielaboradoResponse(e.getId(), e.getFechaElaboracion(), e.getFechaVencimiento(),
                e.getCantidad(), e.getCantidadRestante(), e.getCostoUnitario(), costoTotal, estado, editable);
    }

    private static double redondear(double v) {
        return Math.round(v * 1_000_000d) / 1_000_000d;
    }

    private static String formatear(double v) {
        return BigDecimal.valueOf(redondear(v)).stripTrailingZeros().toPlainString();
    }
}

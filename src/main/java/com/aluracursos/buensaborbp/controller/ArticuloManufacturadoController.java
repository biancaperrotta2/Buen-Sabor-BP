package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoRequest;
import com.aluracursos.buensaborbp.dto.ExistenciaSemielaboradoRequest;
import com.aluracursos.buensaborbp.dto.ExistenciaSemielaboradoResponse;
import com.aluracursos.buensaborbp.dto.PageResponse;
import com.aluracursos.buensaborbp.dto.SemielaboradoAdminResponse;
import com.aluracursos.buensaborbp.service.ExistenciaSemielaboradoService;
import com.aluracursos.buensaborbp.service.ArticuloManufacturadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articulo-manufacturado")
@RequiredArgsConstructor
public class ArticuloManufacturadoController {

    private final ArticuloManufacturadoService articuloManufacturadoService;
    private final ExistenciaSemielaboradoService existenciaService;

    @GetMapping
    public ResponseEntity<List<ArticuloManufacturadoBaseResponse>> listar() {
        return ResponseEntity.ok(articuloManufacturadoService.getAll().getContent());
    }

    // Para la tabla de admin "Productos": búsqueda + paginado + orden reales. Distinto del
    // /articulo-manufacturado de arriba, que sigue alimentando el catálogo público (menú del
    // cliente) sin cambios.
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ArticuloManufacturadoBaseResponse>> listarAdmin(
            @RequestParam(required = false, defaultValue = "") String busqueda,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(articuloManufacturadoService.getAllAdmin(busqueda, pageable));
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ArticuloManufacturadoBaseResponse>> listarActivos() {
        return ResponseEntity.ok(articuloManufacturadoService.getActivos().getContent());
    }

    // Gestor de semielaborados: son insumos internos de otras recetas, no catálogo público.
    @GetMapping("/semielaborados")
    @PreAuthorize("hasAnyRole('ADMIN','COCINERO')")
    public ResponseEntity<List<ArticuloManufacturadoBaseResponse>> listarSemielaborados() {
        return ResponseEntity.ok(articuloManufacturadoService.getSemielaborados().getContent());
    }

    // Tabla "Semielaborados" (historia #21): admin y cocinero.
    @GetMapping("/semielaborados/admin")
    @PreAuthorize("hasAnyRole('ADMIN','COCINERO')")
    public ResponseEntity<PageResponse<SemielaboradoAdminResponse>> listarSemielaboradosAdmin(
            @RequestParam(required = false, defaultValue = "") String busqueda,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(articuloManufacturadoService.getSemielaboradosAdmin(busqueda, pageable));
    }

    // --- Existencias (lotes) de un semielaborado: verlas admin y cocinero; cargarlas,
    // corregirlas, borrarlas o darlas de baja solo el admin (historias #23 y #24). ---

    @GetMapping("/{id}/existencias")
    @PreAuthorize("hasAnyRole('ADMIN','COCINERO')")
    public ResponseEntity<List<ExistenciaSemielaboradoResponse>> listarExistencias(@PathVariable Long id) {
        return ResponseEntity.ok(existenciaService.listar(id));
    }

    @PostMapping("/{id}/existencias")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExistenciaSemielaboradoResponse> crearExistencia(
            @PathVariable Long id, @Valid @RequestBody ExistenciaSemielaboradoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(existenciaService.crear(id, request));
    }

    @PutMapping("/existencias/{existenciaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExistenciaSemielaboradoResponse> editarExistencia(
            @PathVariable Long existenciaId, @Valid @RequestBody ExistenciaSemielaboradoRequest request) {
        return ResponseEntity.ok(existenciaService.editar(existenciaId, request));
    }

    @DeleteMapping("/existencias/{existenciaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarExistencia(@PathVariable Long existenciaId) {
        existenciaService.eliminar(existenciaId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/existencias/{existenciaId}/descartar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExistenciaSemielaboradoResponse> descartarExistencia(@PathVariable Long existenciaId) {
        return ResponseEntity.ok(existenciaService.descartar(existenciaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticuloManufacturadoFullResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(articuloManufacturadoService.getById(id));
    }

    // Crear y editar: admin y cocinero (el enunciado le da al cocinero el catálogo y los
    // semielaborados). Eliminar sigue siendo solo del admin.
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COCINERO')")
    public ResponseEntity<ArticuloManufacturadoFullResponse> crear(@Valid @RequestBody ArticuloManufacturadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(articuloManufacturadoService.crearManufacturado(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COCINERO')")
    public ResponseEntity<ArticuloManufacturadoFullResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ArticuloManufacturadoRequest request) {
        return ResponseEntity.ok(articuloManufacturadoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        articuloManufacturadoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

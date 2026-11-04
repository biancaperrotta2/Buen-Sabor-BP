package com.aluracursos.buensaborbp.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoRequest;
import com.aluracursos.buensaborbp.dto.PageResponse;
import com.aluracursos.buensaborbp.dto.UsoInsumoResponse;
import com.aluracursos.buensaborbp.dto.InsumoMenuResponse;
import com.aluracursos.buensaborbp.service.ArticuloInsumoServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

// Insumos: datos internos de costo/stock, no forman parte del catálogo público. Solo ADMIN.
@RestController
@RequestMapping("/insumos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ArticuloInsumoController {

    private final ArticuloInsumoServiceImpl servicio;

    //GET - Lista todos los insumos, con búsqueda + paginado + orden para la tabla de admin
    // El cocinero también la usa: para elegir los insumos de la receta de un semielaborado.
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','COCINERO')")
    public ResponseEntity<PageResponse<ArticuloInsumoBaseResponse>> listarInsumos(
            @RequestParam(required = false, defaultValue = "") String busqueda,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(servicio.getAll(busqueda, pageable));
    }

    //GET - Lista solo los insumos activos
    @GetMapping("/activos")
    public ResponseEntity<List<ArticuloInsumoBaseResponse>> listarActivos() {
        List<ArticuloInsumoBaseResponse> activos = servicio.getActivos();
        return ResponseEntity.ok(activos);
    }

    // Público (menú del cliente): los insumos que se venden directo, sin costo ni stock. El
    // @PreAuthorize del método reemplaza al ADMIN de la clase; la URL está en permitAll de
    // SecurityConfig.
    @GetMapping("/vendibles")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<InsumoMenuResponse>> listarVendibles() {
        return ResponseEntity.ok(servicio.getVendibles());
    }

    // Recetas y promociones que usan el insumo (para avisar antes de cambiarle la unidad).
    @GetMapping("/{id}/uso")
    public ResponseEntity<UsoInsumoResponse> obtenerUso(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.obtenerUso(id));
    }

    //GET - Devuelvr un insumo por ID
    @GetMapping("/{id}")
    public ResponseEntity<ArticuloInsumoFullResponse> obtenerPorId(@PathVariable Long id) {
        ArticuloInsumoFullResponse response = servicio.getById(id);
        return ResponseEntity.ok(response);
    }

    //POST - Crea un nuevo insumo
    @PostMapping
    public ResponseEntity<ArticuloInsumoFullResponse> crearInsumo(@Valid @RequestBody ArticuloInsumoRequest request) {
        ArticuloInsumoFullResponse creado = servicio.crearArticuloInsumo(request);
        return ResponseEntity.ok(creado);
    }

    //PUT - Actualiza un insumo existente
    @PutMapping("/{id}")
    public ResponseEntity<ArticuloInsumoFullResponse> actualizarInsumo(
            @PathVariable Long id,
            @Valid @RequestBody ArticuloInsumoRequest request) {

        ArticuloInsumoFullResponse actualizado = servicio.update(id, request);
        return ResponseEntity.ok(actualizado);
    }

    //DELETE - Elimina un insumo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarInsumo(@PathVariable Long id) {
        servicio.delete(id);
        return ResponseEntity.noContent().build();
    }
}

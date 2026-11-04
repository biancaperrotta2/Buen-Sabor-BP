package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.CategoriaBaseResponse;
import com.aluracursos.buensaborbp.dto.CategoriaFullResponse;
import com.aluracursos.buensaborbp.dto.CategoriaRequest;
import com.aluracursos.buensaborbp.dto.PageResponse;
import com.aluracursos.buensaborbp.service.CategoriaService;
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
@RequestMapping("/categoria")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping("/all")
    public ResponseEntity<List<CategoriaBaseResponse>> listar() {
        return ResponseEntity.ok(categoriaService.mostrarCategorias());
    }

    // Para la tabla de admin (búsqueda + paginado + orden). No confundir con /all: ese lo
    // siguen usando los <select> de categoría de los formularios, que necesitan la lista
    // completa sin paginar.
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<CategoriaBaseResponse>> listarAdmin(
            @RequestParam(required = false, defaultValue = "") String busqueda,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(categoriaService.mostrarCategoriasAdmin(busqueda, pageable));
    }

    @GetMapping("/hijas/{padreId}")
    public ResponseEntity<List<CategoriaBaseResponse>> listarHijas(@PathVariable Long padreId) {
        return ResponseEntity.ok(categoriaService.listarHijas(padreId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaBaseResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarCategoriaID(id));
    }

    @PostMapping("/crear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaFullResponse> crear(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crearCategoria(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaFullResponse> actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(categoriaService.actualizarCategoria(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.desactivarCategoria(id);
        return ResponseEntity.noContent().build();
    }
}

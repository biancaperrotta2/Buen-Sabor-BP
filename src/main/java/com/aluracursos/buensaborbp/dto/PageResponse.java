package com.aluracursos.buensaborbp.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

// Envoltorio genérico para toda tabla de admin con búsqueda + paginado + orden real de
// backend (Spring Pageable). "pagina" es 0-based, igual que Spring Data, para no tener que
// traducir índices entre el front y el back.
public record PageResponse<T>(
    List<T> contenido,
    int pagina,
    int tamanio,
    long totalElementos,
    int totalPaginas
) {
    public static <T> PageResponse<T> de(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    public static <E, T> PageResponse<T> de(Page<E> page, Function<E, T> mapeo) {
        return de(page.map(mapeo));
    }
}

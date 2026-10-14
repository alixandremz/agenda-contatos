package com.alixandremz.agenda.dto;

import java.util.List;

import org.springframework.data.domain.Page;

// Invólucro próprio em vez de devolver o Page do Spring direto: o JSON fica
// estável e não dependemos do formato interno do PageImpl.
public record PaginaResponse<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long total,
        int totalPaginas
) {
    public static <T> PaginaResponse<T> de(Page<T> pagina) {
        return new PaginaResponse<>(
                pagina.getContent(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }
}

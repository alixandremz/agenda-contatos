package com.alixandremz.agenda.dto;

import java.time.Instant;

import com.alixandremz.agenda.model.Contato;

public record ContatoResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static ContatoResponse de(Contato contato) {
        return new ContatoResponse(
                contato.getId(),
                contato.getNome(),
                contato.getEmail(),
                contato.getTelefone(),
                contato.getCriadoEm(),
                contato.getAtualizadoEm()
        );
    }
}

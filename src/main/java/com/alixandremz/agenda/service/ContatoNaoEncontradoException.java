package com.alixandremz.agenda.service;

public class ContatoNaoEncontradoException extends RuntimeException {

    public ContatoNaoEncontradoException(Long id) {
        super("contato não encontrado: id " + id);
    }
}

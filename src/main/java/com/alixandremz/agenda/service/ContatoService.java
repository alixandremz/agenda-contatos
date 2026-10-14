package com.alixandremz.agenda.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alixandremz.agenda.dto.ContatoRequest;
import com.alixandremz.agenda.dto.ContatoResponse;
import com.alixandremz.agenda.dto.PaginaResponse;
import com.alixandremz.agenda.model.Contato;
import com.alixandremz.agenda.repository.ContatoRepository;

@Service
public class ContatoService {

    private final ContatoRepository repositorio;

    public ContatoService(ContatoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ContatoResponse> listar(String nome, Pageable pageable) {
        Page<Contato> pagina = (nome == null || nome.isBlank())
                ? repositorio.findAll(pageable)
                : repositorio.findByNomeContainingIgnoreCase(nome.trim(), pageable);
        return PaginaResponse.de(pagina.map(ContatoResponse::de));
    }

    @Transactional(readOnly = true)
    public ContatoResponse buscar(Long id) {
        return ContatoResponse.de(obter(id));
    }

    @Transactional
    public ContatoResponse criar(ContatoRequest pedido) {
        Contato contato = new Contato();
        aplicar(contato, pedido);
        return ContatoResponse.de(repositorio.save(contato));
    }

    @Transactional
    public ContatoResponse atualizar(Long id, ContatoRequest pedido) {
        Contato contato = obter(id);
        aplicar(contato, pedido);
        return ContatoResponse.de(repositorio.save(contato));
    }

    @Transactional
    public void remover(Long id) {
        repositorio.delete(obter(id));
    }

    private Contato obter(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new ContatoNaoEncontradoException(id));
    }

    private void aplicar(Contato contato, ContatoRequest pedido) {
        contato.setNome(pedido.nome().trim());
        contato.setEmail(pedido.email());
        contato.setTelefone(pedido.telefone());
    }
}

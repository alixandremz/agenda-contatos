package com.alixandremz.agenda.web;

import java.net.URI;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alixandremz.agenda.dto.ContatoRequest;
import com.alixandremz.agenda.dto.ContatoResponse;
import com.alixandremz.agenda.dto.PaginaResponse;
import com.alixandremz.agenda.service.ContatoService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/contatos")
@Validated
public class ContatoController {

    private final ContatoService servico;

    public ContatoController(ContatoService servico) {
        this.servico = servico;
    }

    @GetMapping
    public PaginaResponse<ContatoResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "a página não pode ser negativa") int pagina,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "o tamanho mínimo é 1")
            @Max(value = 100, message = "o tamanho máximo é 100") int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.ASC, "nome"));
        return servico.listar(nome, pageable);
    }

    @GetMapping("/{id}")
    public ContatoResponse buscar(@PathVariable Long id) {
        return servico.buscar(id);
    }

    @PostMapping
    public ResponseEntity<ContatoResponse> criar(@Valid @RequestBody ContatoRequest pedido) {
        ContatoResponse criado = servico.criar(pedido);
        return ResponseEntity.created(URI.create("/contatos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public ContatoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ContatoRequest pedido) {
        return servico.atualizar(id, pedido);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        servico.remover(id);
        return ResponseEntity.noContent().build();
    }
}

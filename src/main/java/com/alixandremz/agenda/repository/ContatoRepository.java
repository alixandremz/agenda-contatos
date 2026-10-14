package com.alixandremz.agenda.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.alixandremz.agenda.model.Contato;

public interface ContatoRepository extends JpaRepository<Contato, Long> {

    // busca parcial (contém) e ignorando maiúsculas/minúsculas
    Page<Contato> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}

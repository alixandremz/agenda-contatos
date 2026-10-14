package com.alixandremz.agenda.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContatoRequest(
        @NotBlank(message = "o nome é obrigatório")
        @Size(max = 120, message = "o nome deve ter no máximo 120 caracteres")
        String nome,

        @Email(message = "email inválido")
        @Size(max = 180, message = "o email deve ter no máximo 180 caracteres")
        String email,

        @Size(max = 30, message = "o telefone deve ter no máximo 30 caracteres")
        String telefone
) {
}

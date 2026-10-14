package com.alixandremz.agenda.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import com.alixandremz.agenda.service.ContatoNaoEncontradoException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ContatoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(ContatoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErroResposta.de(ex.getMessage()));
    }

    // erros de validação do corpo (@Valid no @RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> invalido(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.put(erro.getField(), erro.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErroResposta("dados inválidos", campos));
    }

    // erros de validação de parâmetros de query (@Validated no controller)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResposta> parametroInvalido(ConstraintViolationException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getConstraintViolations()
                .forEach(erro -> campos.put(erro.getPropertyPath().toString(), erro.getMessage()));
        return ResponseEntity.badRequest().body(new ErroResposta("dados inválidos", campos));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErroResposta> parametroInvalido(HandlerMethodValidationException ex) {
        return ResponseEntity.badRequest().body(ErroResposta.de("parâmetros da requisição inválidos"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> corpoInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(ErroResposta.de("corpo da requisição inválido"));
    }
}

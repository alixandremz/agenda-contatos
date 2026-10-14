package com.alixandremz.agenda;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.alixandremz.agenda.repository.ContatoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ContatoApiTests {

    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ContatoRepository repositorio;

    @BeforeEach
    void limpar() {
        repositorio.deleteAll();
    }

    private long criarContato(String nome) throws Exception {
        String resposta = mvc.perform(post("/contatos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"email\":\"" + nome.toLowerCase() + "@exemplo.com\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Matcher matcher = ID.matcher(resposta);
        matcher.find();
        return Long.parseLong(matcher.group(1));
    }

    @Test
    void criaContatoComStatus201() throws Exception {
        mvc.perform(post("/contatos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana Silva\",\"email\":\"ana@exemplo.com\",\"telefone\":\"11999998888\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.email").value("ana@exemplo.com"))
                .andExpect(jsonPath("$.telefone").value("11999998888"));
    }

    @Test
    void buscaContatoPorId() throws Exception {
        long id = criarContato("Bruno");
        mvc.perform(get("/contatos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bruno"));
    }

    @Test
    void buscaContatoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/contatos/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void listaComPaginacao() throws Exception {
        criarContato("Ana");
        criarContato("Bruno");
        criarContato("Carla");

        mvc.perform(get("/contatos").param("tamanho", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(2))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2))
                .andExpect(jsonPath("$.conteudo.length()").value(2));
    }

    @Test
    void buscaPorNomeIgnoraMaiusculas() throws Exception {
        criarContato("Ana");
        criarContato("Bruno");

        mvc.perform(get("/contatos").param("nome", "AN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.conteudo[0].nome").value("Ana"));
    }

    @Test
    void atualizaContato() throws Exception {
        long id = criarContato("Ana");
        mvc.perform(put("/contatos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana Souza\",\"telefone\":\"1133334444\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.telefone").value("1133334444"));
    }

    @Test
    void removeContato() throws Exception {
        long id = criarContato("Ana");
        mvc.perform(delete("/contatos/{id}", id))
                .andExpect(status().isNoContent());
        mvc.perform(get("/contatos/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void nomeEmBrancoRetorna400() throws Exception {
        mvc.perform(post("/contatos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists());
    }

    @Test
    void emailInvalidoRetorna400() throws Exception {
        mvc.perform(post("/contatos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"sem-arroba\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists());
    }

    @Test
    void tamanhoDePaginaInvalidoRetorna400() throws Exception {
        mvc.perform(get("/contatos").param("tamanho", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());
    }
}

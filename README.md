# agenda-contatos

API REST de **agenda de contatos** criada para **estudar Spring Boot e Spring Web**.
Foco deste projeto: um CRUD completo com **busca por nome** e **paginação**, que são
os dois recursos mais usados em APIs reais.

Cada contato tem nome (obrigatório), email e telefone (opcionais). A listagem vem
paginada e pode ser filtrada por parte do nome.

---

## O que estou estudando aqui

| Área | Conceitos praticados |
|------|----------------------|
| Spring Web (REST) | `@RestController`, `@RequestMapping`, `@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping`, `@PathVariable`, `@RequestBody`, `@RequestParam`, `ResponseEntity`, códigos HTTP (200, 201, 204, 400, 404) |
| CRUD | criar, buscar por id, atualizar, remover |
| Paginação | `Pageable`, `PageRequest`, `Sort`, `Page` do Spring Data |
| Busca | query derivada `findByNomeContainingIgnoreCase` (busca parcial e sem diferenciar maiúsculas) |
| Spring Data JPA | entidade `@Entity`, `JpaRepository`, métodos de consulta derivados do nome |
| Bean Validation | `@NotBlank`, `@Size`, `@Email`, `@Valid`, `@Min`/`@Max` em parâmetros de query, `@Validated` no controller |
| Tratamento de erros | `@RestControllerAdvice`, `@ExceptionHandler`, formato de erro padrão |
| DTOs | `record` de request/response, factory method, invólucro de paginação próprio |
| Testes | `@SpringBootTest`, `MockMvc`, `jsonPath`, testes de sucesso e de erro |

---

## Stack

- Java 21
- Spring Boot 4.1
- Spring Web MVC, Spring Data JPA, Bean Validation
- H2 em memória
- Maven (via Maven Wrapper `./mvnw`, não precisa ter Maven instalado)

---

## Arquitetura

Organização em camadas, cada uma com uma responsabilidade:

```
HTTP  ──►  web (Controller)  ──►  service (regras)  ──►  repository (JPA)  ──►  banco
                 │                      │                      │
                 ▼                      ▼                      ▼
          ErroResposta /          ContatoResponse        Contato (entidade)
          RestExceptionHandler    PaginaResponse         tabela "contatos"
```

- **web** — recebe a requisição, valida a entrada e devolve JSON. Concentra os erros.
- **service** — regras de negócio e orquestração (montar a página, aplicar busca, CRUD).
- **repository** — acesso ao banco via Spring Data JPA.
- **model** — entidade `Contato` (tabela `contatos`).
- **dto** — entrada/saída (`ContatoRequest`, `ContatoResponse`, `PaginaResponse`).

### Estrutura de pastas

```
src/main/java/com/alixandremz/agenda/
├── AgendaContatosApplication.java
├── model/
│   └── Contato.java                # entidade (tabela contatos)
├── repository/
│   └── ContatoRepository.java      # JpaRepository + busca por nome
├── service/
│   ├── ContatoService.java         # CRUD + listagem paginada
│   └── ContatoNaoEncontradoException.java
├── dto/
│   ├── ContatoRequest.java         # entrada (com validação)
│   ├── ContatoResponse.java        # saída
│   └── PaginaResponse.java         # invólucro de página
└── web/
    ├── ContatoController.java      # endpoints
    ├── ErroResposta.java           # formato padrão de erro
    └── RestExceptionHandler.java   # @RestControllerAdvice
```

---

## Endpoints

| Método | Caminho | Descrição | Sucesso | Erros |
|--------|---------|-----------|---------|-------|
| GET | `/contatos` | lista paginada; filtro opcional por `nome` | `200 OK` | `400` parâmetros inválidos |
| GET | `/contatos/{id}` | busca um contato | `200 OK` | `404` |
| POST | `/contatos` | cria um contato | `201 Created` | `400` validação |
| PUT | `/contatos/{id}` | atualiza um contato | `200 OK` | `400`, `404` |
| DELETE | `/contatos/{id}` | remove um contato | `204 No Content` | `404` |

### Listagem com paginação e busca

Parâmetros de query:

| Parâmetro | Padrão | Regra |
|-----------|--------|-------|
| `nome` | (vazio) | filtra por parte do nome, sem diferenciar maiúsculas |
| `pagina` | `0` | `>= 0` |
| `tamanho` | `10` | entre `1` e `100` |

Resultado sempre ordenado por nome.

```bash
# primeira página, 10 por vez
curl "http://localhost:8080/contatos"

# busca por nome, página 0, 5 por página
curl "http://localhost:8080/contatos?nome=ana&pagina=0&tamanho=5"
```

O JSON da página tem formato próprio (não expõe o `Page` interno do Spring):

```json
{
  "conteudo": [
    { "id": 1, "nome": "Ana Silva", "email": "ana@exemplo.com", "telefone": "11999998888",
      "criadoEm": "2026-10-14T17:40:00Z", "atualizadoEm": "2026-10-14T17:40:00Z" }
  ],
  "pagina": 0,
  "tamanho": 5,
  "total": 1,
  "totalPaginas": 1
}
```

### Criar

```bash
curl -X POST http://localhost:8080/contatos \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Ana Silva","email":"ana@exemplo.com","telefone":"11999998888"}'
```

Erro de validação (`400 Bad Request`):

```json
{
  "erro": "dados inválidos",
  "campos": {
    "nome": "o nome é obrigatório",
    "email": "email inválido"
  }
}
```

### Atualizar

```bash
curl -X PUT http://localhost:8080/contatos/1 \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Ana Souza","telefone":"1133334444"}'
```

### Remover

```bash
curl -i -X DELETE http://localhost:8080/contatos/1
# HTTP/1.1 204 No Content
```

---

## Decisões e detalhes técnicos

- **Paginação com `PageRequest` montado na mão.** Em vez de receber um `Pageable` cru
  (que usaria `page`/`size` em inglês), o controller lê `pagina` e `tamanho`, valida os
  limites (`@Min`/`@Max`) e constrói `PageRequest.of(pagina, tamanho, Sort.by("nome"))`.
  Assim os nomes ficam em português e a ordenação é fixa e previsível.
- **Invólucro de paginação próprio (`PaginaResponse`).** Devolver o `Page` do Spring
  direto acopla o JSON ao formato interno dele e gera aviso de serialização. O
  `PaginaResponse` expõe só o que interessa: `conteudo`, `pagina`, `tamanho`, `total`
  e `totalPaginas`.
- **Busca por nome.** Se `nome` vier vazio, lista tudo; senão usa
  `findByNomeContainingIgnoreCase` — que o Spring Data monta sozinho a partir do nome
  do método (`Containing` = `LIKE %...%`, `IgnoreCase` = comparação sem caixa).
- **Validação em dois lugares.** O corpo (`@RequestBody`) é validado com `@Valid`, e os
  parâmetros de query com `@Validated` no controller + `@Min`/`@Max`. Os dois viram `400`
  no `RestExceptionHandler`, no mesmo formato de erro.
- **Entity vs DTO.** A entidade `Contato` nunca vai direto na resposta; sempre passa pelo
  `ContatoResponse`. Isso evita vazar detalhes internos e dá liberdade para mudar o banco
  sem quebrar a API.
- **`criadoEm`/`atualizadoEm` automáticos** via `@PrePersist` e `@PreUpdate`.

---

## Como rodar

Usa H2 em memória; os dados somem ao parar a aplicação.

```bash
cd agenda-contatos
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. O console do H2 fica em
`http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:agenda`, usuário `sa`, sem senha).

Também há um `Dockerfile` (multi-stage, rodando com usuário sem privilégios) caso queira
empacotar:

```bash
docker build -t agenda-contatos .
docker run -p 8080:8080 agenda-contatos
```

---

## Testes

```bash
./mvnw test
```

Cobrem:

- criação com `201` e `Location`;
- busca por id (sucesso e `404`);
- listagem paginada (`total`, `totalPaginas`, tamanho da página);
- busca por nome ignorando maiúsculas;
- atualização;
- remoção (`204` e depois `404`);
- nome em branco e email inválido → `400` com o campo de erro;
- tamanho de página inválido → `400`.

---

## Próximos passos (ideias para continuar estudando)

- Ordenação configurável (`ordenarPor`/`direcao` por query param).
- Migrations com **Flyway** em vez de `ddl-auto=update`.
- Trocar H2 por **PostgreSQL** + `docker-compose`.
- Validação de telefone com `@Pattern` e normalização (só dígitos).
- Email único (constraint no banco) e tratamento de `409 Conflict`.
- **Spring Security + JWT** para proteger a agenda por usuário.
- Documentação automática com **OpenAPI / Swagger**.

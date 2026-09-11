# Estoquecontrol

API para cadastro de produtos e lançamentos de entrada e saída de estoque, adaptada do material de [Jefferson Ar Passerini — aulas 00 a 07](https://github.com/jeffersonarpasserini/suporteos2026/tree/main/docs).

## Tecnologias e organização

Java 21, Spring Boot 4.0.7, Maven Wrapper 3.9.16, Spring Web MVC, Bean Validation, Spring Data JPA, PostgreSQL e Liquibase 5.0.3. Servidor incorporado, sem necessidade de Tomcat externo.

Fluxo: controller → DTO/mapeador manual → serviço transacional → entidade/repository → PostgreSQL. O Liquibase cria as tabelas; Hibernate usa `ddl-auto=validate`. Testes também utilizam PostgreSQL, em banco separado.

| Aula | Aplicação no projeto |
|---|---|
| 00 | Git, `.gitignore`, `.editorconfig`, commits, branch `main` e tag de entrega |
| 01 | Java 21, Maven e servidor incorporado |
| 02 | Projeto Spring Boot, tema próprio e `/api/health` |
| 03 | Domínio encapsulado, enum, `BigDecimal`, regras e testes unitários |
| 04 | JPA, PostgreSQL, perfis e migrações Liquibase |
| 05 | Repositories, injeção por construtor, serviços e transações |
| 06 | Fornecedor opcional, estoque mínimo e evolução incremental do esquema |
| 07 | DTOs, validações, mapeadores, REST, erros, MockMvc e Postman |

Grupo de produto e fornecedor seguem o modelo das aulas. O lançamento de estoque adapta as operações `receberEstoque` e `retirarEstoque` ao requisito deste projeto, com histórico e bloqueio do produto durante a transação.

## Banco e configuração

Use PostgreSQL em `localhost:5432`, com usuário **postgres** e a senha local definida para o projeto. Nesta entrega o `.env` local já está preenchido com a senha solicitada. Ele é ignorado pelo Git, conforme as aulas. Em outro computador, copie `.env.example` para `.env` e preencha `DB_DEV_PASSWORD` e `DB_TEST_PASSWORD` com a mesma senha.

Crie os bancos com o cliente `psql` (ele solicitará a senha):

```powershell
& 'C:\Program Files\PostgreSQL\14\bin\psql.exe' -h localhost -U postgres -d postgres -W -v ON_ERROR_STOP=1 -f scripts/criar-bancos.sql
```

Ajuste o caminho do executável se sua versão do PostgreSQL for outra. O script cria apenas bancos ausentes, sem excluir dados.

- Desenvolvimento: `estoquecontrol_dev`.
- Testes: `estoquecontrol_test`.
- Tabelas de negócio: `grupo_produto`, `fornecedor`, `produto`, `lancamento_estoque`.
- Controle Liquibase: `historico_migracao` e `bloqueio_migracao`.

As colunas de negócio também estão em português. As colunas internas das duas tabelas do Liquibase seguem o formato gerenciado pela ferramenta.

## Executar

Abra o terminal na raiz deste projeto:

```powershell
.\mvnw.cmd '-Dspring-boot.run.profiles=dev' spring-boot:run
```

Ou execute `iniciar.ps1`. No IntelliJ, abra o `pom.xml`, selecione JDK 21 e configure o perfil ativo `dev` na classe `EstoquecontrolApplication`. O diretório de trabalho deve ser a raiz do projeto para carregar `.env`.

Depois da mensagem de inicialização, a API responde em **http://localhost:8080**. `GET /api/health` retorna `200` e o texto `OK`. Mantenha o processo Java em execução enquanto usar o Postman.

Para gerar e executar o JAR:

```powershell
.\mvnw.cmd clean verify
java -jar target/estoquecontrol-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

No Linux/macOS, use `./mvnw` no lugar de `.\mvnw.cmd`. O perfil `prod` recebe `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` do ambiente; use o mesmo usuário e senha solicitados. GitHub hospeda o código, não o processo da API.

## Testar no Postman

Importe **`postman/estoquecontrol.postman_collection.json`** e execute a coleção na ordem. A variável `baseUrl` já vale `http://localhost:8080`. O roteiro cria grupo e produto, captura os IDs, lança entrada de 10, saída de 3 e confirma saldo 7. Também testa os erros `400`, `404` e `409` e verifica que não alteraram o saldo/histórico.

Cada execução cria novos códigos únicos, permitindo repetir o roteiro sem limpar o banco. Fornecedor é opcional; pode ser cadastrado separadamente e informado por `fornecedorId` no produto.

## Rotas

| Método | Rota | Finalidade |
|---|---|---|
| GET | `/api/health` | Verificar resposta da aplicação |
| POST / GET | `/api/grupos-produtos` | Cadastrar / listar grupos |
| GET | `/api/grupos-produtos/{id}` | Consultar grupo |
| POST / GET | `/api/fornecedores` | Cadastrar / listar fornecedores |
| GET | `/api/fornecedores/{id}` | Consultar fornecedor |
| POST / GET | `/api/produtos` | Cadastrar / listar produtos |
| GET | `/api/produtos/{id}` | Consultar produto e saldo |
| GET | `/api/produtos/estoque-baixo` | Listar produtos com saldo abaixo do mínimo |
| POST / GET | `/api/lancamentos-estoque` | Registrar / listar lançamentos |
| GET | `/api/lancamentos-estoque/{id}` | Consultar lançamento |
| GET | `/api/lancamentos-estoque?produtoId={id}` | Histórico de um produto |

### Exemplos de corpos JSON

`POST /api/grupos-produtos`:

```json
{"nome":"Periféricos"}
```

`POST /api/fornecedores` (opcional):

```json
{"razaoSocial":"Distribuidora Exemplo","cnpj":"12345678000199"}
```

O CNPJ segue a simplificação da aula: 14 dígitos, sem cálculo dos dígitos verificadores.

`POST /api/produtos` — substitua `grupoId` pelo ID recebido:

```json
{
  "codigoBarras":"MOUSE-001",
  "descricao":"Mouse sem fio",
  "valorUnitario":89.90,
  "estoqueMinimo":2,
  "grupoId":1
}
```

O produto inicia com saldo zero. O campo `saldoEstoque` é somente de resposta: enviá-lo no cadastro retorna `400`. Para informar um saldo inicial, registre uma entrada.

`POST /api/lancamentos-estoque`:

```json
{
  "produtoId":1,
  "tipo":"ENTRADA",
  "quantidade":10,
  "observacao":"Compra inicial"
}
```

Para saída, use `"tipo":"SAIDA"` e quantidade positiva. A resposta inclui `id`, `produtoId`, `tipo`, `quantidade`, `saldoAnterior`, `saldoPosterior`, `dataLancamento` e `observacao`.

## Regras e respostas

- Quantidade deve ser positiva, com até 3 casas decimais. Valores monetários aceitam até 2 casas.
- Saída não pode ultrapassar o saldo disponível; retirada exata pode zerá-lo.
- Saldo e lançamento são confirmados juntos; uma falha desfaz a operação.
- Movimentos simultâneos do mesmo produto aguardam o bloqueio transacional, evitando consumir o mesmo saldo duas vezes.
- Código de barras é único. Grupo é obrigatório; fornecedor é opcional.
- Não há edição ou exclusão de lançamentos. Cada POST válido registra uma nova operação; reenviar a mesma entrada registra outra entrada.
- Campos desconhecidos e JSON malformado retornam `400`.

| Código | Uso |
|---|---|
| 200 | Consulta/listagem bem-sucedida |
| 201 | Cadastro/lançamento criado, com cabeçalho `Location` |
| 400 | Entrada inválida |
| 404 | Recurso não encontrado |
| 409 | Duplicidade, saldo insuficiente ou conflito de integridade |

Erros seguem o contrato da aula: `timestamp`, `status`, `error`, `message`, `path` e `fields`.

## Testes e migrações

```powershell
.\mvnw.cmd test
```

A suíte cobre domínio, persistência, validação, cadastro, saldo, histórico, rollback e saídas concorrentes. Testes HTTP usam transações reais por requisição, permitindo detectar falhas de commit. Os dados dos testes ficam exclusivamente no banco `estoquecontrol_test`.

As migrações 001–003 preservam o encadeamento didático; a 004 cria os lançamentos. Não altere migrações já aplicadas: acrescente uma nova. O perfil `schema-reference` e o plugin `liquibase:diff` estão disponíveis como na aula 06, exclusivamente para bancos descartáveis `estoquecontrol_reference` e `estoquecontrol_diff`, criados separadamente. Não são necessários para executar a API.

## Referência e escopo

Base didática adaptada do repositório `jeffersonarpasserini/suporteos2026`, com autoria das migrações originais preservada. O projeto oferece cadastro, consulta e lançamentos pelo padrão REST das aulas 00–07; não inclui telas, autenticação, vendas ou recursos de aulas posteriores.

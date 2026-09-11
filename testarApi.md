# Testar a API Estoquecontrol

Use o Postman com a aplicação executando em:

```text
http://localhost:8080
```

Nas requisições `POST`, selecione **Body > raw > JSON**. Os números de ID usados abaixo são exemplos. Substitua pelos IDs retornados pela sua API.

## 1. Verificar se a API está funcionando

**Método:** `GET`

**URL:** `http://localhost:8080/api/health`

Resposta esperada: status `200 OK` e texto `OK`.

## 2. Cadastrar um grupo de produtos

**Método:** `POST`

**URL:** `http://localhost:8080/api/grupos-produtos`

```json
{
  "nome": "Periféricos"
}
```

Resposta esperada: `201 Created`. Guarde o valor de `id` retornado.

## 3. Consultar grupos de produtos

Consultar todos:

**Método:** `GET`

**URL:** `http://localhost:8080/api/grupos-produtos`

Consultar um grupo pelo ID:

**Método:** `GET`

**URL:** `http://localhost:8080/api/grupos-produtos/1`

## 4. Cadastrar um fornecedor

**Método:** `POST`

**URL:** `http://localhost:8080/api/fornecedores`

```json
{
  "razaoSocial": "Distribuidora Exemplo Ltda",
  "cnpj": "12345678000199"
}
```

O CNPJ deve possuir 14 dígitos. Resposta esperada: `201 Created`. Guarde o `id` retornado.

## 5. Consultar fornecedores

Consultar todos:

**Método:** `GET`

**URL:** `http://localhost:8080/api/fornecedores`

Consultar um fornecedor pelo ID:

**Método:** `GET`

**URL:** `http://localhost:8080/api/fornecedores/1`

## 6. Cadastrar um produto

Cadastre primeiro o grupo e o fornecedor para obter seus IDs.

**Método:** `POST`

**URL:** `http://localhost:8080/api/produtos`

```json
{
  "codigoBarras": "MOUSE-001",
  "descricao": "Mouse sem fio",
  "valorUnitario": 89.90,
  "estoqueMinimo": 2.000,
  "grupoId": 1,
  "fornecedorId": 1
}
```

O `grupoId` é obrigatório. O `fornecedorId` é opcional e pode ser removido do JSON. O produto sempre começa com saldo zero.

Resposta esperada: `201 Created`. Guarde o `id` do produto.

## 7. Consultar produtos

Consultar todos:

**Método:** `GET`

**URL:** `http://localhost:8080/api/produtos`

Consultar um produto pelo ID:

**Método:** `GET`

**URL:** `http://localhost:8080/api/produtos/1`

Consultar produtos com saldo abaixo do estoque mínimo:

**Método:** `GET`

**URL:** `http://localhost:8080/api/produtos/estoque-baixo`

São retornados somente os produtos em que `saldoEstoque` é menor que `estoqueMinimo`. Produtos com saldo exatamente igual ao mínimo não aparecem nessa consulta.

## 8. Registrar uma entrada no estoque

**Método:** `POST`

**URL:** `http://localhost:8080/api/lancamentos-estoque`

```json
{
  "produtoId": 1,
  "tipo": "ENTRADA",
  "quantidade": 10.000,
  "observacao": "Compra inicial"
}
```

Resposta esperada: `201 Created`. O retorno mostra `saldoAnterior` igual a zero e `saldoPosterior` igual a 10.

## 9. Registrar uma saída do estoque

**Método:** `POST`

**URL:** `http://localhost:8080/api/lancamentos-estoque`

```json
{
  "produtoId": 1,
  "tipo": "SAIDA",
  "quantidade": 3.000,
  "observacao": "Saída de mercadoria"
}
```

Depois da entrada de 10, essa saída deixa o saldo em 7. Uma saída maior que o saldo disponível retorna `409 Conflict` e não altera o estoque.

## 10. Consultar lançamentos de estoque

Consultar todos os lançamentos:

**Método:** `GET`

**URL:** `http://localhost:8080/api/lancamentos-estoque`

Consultar um lançamento pelo ID:

**Método:** `GET`

**URL:** `http://localhost:8080/api/lancamentos-estoque/1`

Consultar somente o histórico de um produto:

**Método:** `GET`

**URL:** `http://localhost:8080/api/lancamentos-estoque?produtoId=1`

## 11. Atualizar um cadastro

A versão atual da API não possui rotas `PUT` ou `PATCH`. Portanto, grupo, fornecedor e produto ainda não podem ser atualizados pelo Postman.

Por exemplo, esta requisição não está implementada:

**Método:** `PUT`

**URL:** `http://localhost:8080/api/produtos/1`

```json
{
  "descricao": "Mouse sem fio atualizado",
  "valorUnitario": 99.90,
  "estoqueMinimo": 3.000
}
```

O resultado atual será `405 Method Not Allowed`. Para disponibilizar atualização seria necessário implementar o DTO, o método transacional do serviço e a rota `PUT` ou `PATCH`. Isso não foi acrescentado porque o projeto foi limitado às funcionalidades trabalhadas nas aulas 00 a 07.

## 12. Testes rápidos de erro

### Código de barras repetido

Repita o cadastro do mesmo produto com `"codigoBarras": "MOUSE-001"`.

Resposta esperada: `409 Conflict`.

### Quantidade inválida

**Método:** `POST`

**URL:** `http://localhost:8080/api/lancamentos-estoque`

```json
{
  "produtoId": 1,
  "tipo": "ENTRADA",
  "quantidade": 0
}
```

Resposta esperada: `400 Bad Request`.

### Produto inexistente

**Método:** `GET`

**URL:** `http://localhost:8080/api/produtos/999999999`

Resposta esperada: `404 Not Found`.

## Ordem recomendada

1. Verificar o health check.
2. Cadastrar um grupo.
3. Cadastrar um fornecedor.
4. Cadastrar um produto usando os IDs retornados.
5. Registrar uma entrada.
6. Registrar uma saída.
7. Consultar o produto e verificar o saldo.
8. Consultar o histórico de lançamentos.

Ao repetir os testes, altere o nome do grupo, o CNPJ e o código de barras, pois esses valores devem ser únicos.

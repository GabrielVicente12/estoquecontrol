# Tema do projeto

## Identificação

- Repositório: estoquecontrol.
- Domínio: controle simples de estoque.
- Pacote: com.curso.estoquecontrol.

## Entidades

- GrupoProduto: classificação identificada por nome único, relacionada a vários produtos.
- Produto: código de barras único, descrição, saldo, valor unitário, estoque mínimo, data de cadastro e status.
- Fornecedor: razão social, CNPJ de 14 dígitos e status; relacionamento opcional com produtos, conforme aula 06.
- LancamentoEstoque: entrada ou saída, produto, quantidade, saldos anterior/posterior, data e observação opcional.

## Relacionamentos

GrupoProduto 1:N Produto; Fornecedor 1:N Produto (opcional do lado do produto); Produto 1:N LancamentoEstoque.

## Exemplos

Periféricos → Mouse sem fio; Cabos → Cabo HDMI; Papelaria → Resma de papel. Cada produto começa com saldo zero e recebe saldo por lançamentos de entrada.

## Limite do projeto

Cadastro e consulta no padrão das aulas 00–07, com lançamentos exigidos pelo tema. Sem vendas, usuários, telas ou relatórios adicionais.

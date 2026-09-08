# Verificação da entrega

Verificado em 07/09/2026, com Java 21.0.12, Maven 3.9.16 e PostgreSQL 14.5 local.

- `mvn clean verify`: código de saída 0; 37 testes, nenhuma falha ou erro.
- `mvnw.cmd -version`: Wrapper executado, Maven 3.9.16 e Java 21.
- Coleção Postman executada por Newman contra `http://localhost:8080`: 16 requisições e 23 verificações, nenhuma falha.
- Entrada de 10 e saída de 3: saldo 7 e dois lançamentos.
- Saída acima do saldo: HTTP 409, saldo e histórico preservados.
- Duas saídas concorrentes de 7 para saldo 10: uma 201, outra 409, saldo final 3.
- Reinicialização do JAR: produto, saldo 7 e dois lançamentos preservados no PostgreSQL.
- Liquibase aplicado em banco de desenvolvimento inicialmente vazio; seis tabelas em português.
- Revisão estática independente do código e migrações: nenhum problema material encontrado.

Os testes de domínio e persistência da base didática foram preservados e adaptados ao projeto. Os testes adicionais da API de estoque usam transações reais por requisição, sem uma transação externa mascarando falhas de commit.

O `.env` contém as credenciais locais solicitadas e está ignorado pelo Git. Relatórios brutos, logs, dependências, bancos e JAR gerado não são enviados ao repositório.

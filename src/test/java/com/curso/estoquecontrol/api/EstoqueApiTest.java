package com.curso.estoquecontrol.api;

import com.curso.estoquecontrol.domain.GrupoProduto;
import com.curso.estoquecontrol.repository.GrupoProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Sem transação no teste: cada requisição deve confirmar/recusar sua própria transação.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstoqueApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired GrupoProdutoRepository grupos;

    long cadastrar() throws Exception {
        long grupo = grupos.save(new GrupoProduto("Grupo " + UUID.randomUUID())).getId();
        String body = """
                {"codigoBarras":"%s","descricao":"Produto teste","valorUnitario":12.50,
                 "estoqueMinimo":2,"grupoId":%d}
                """.formatted(UUID.randomUUID(), grupo);
        String resposta = mvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.saldoEstoque").value(0))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).get("id").asLong();
    }

    String movimento(long produto, String tipo, String quantidade) {
        return "{\"produtoId\":" + produto + ",\"tipo\":\"" + tipo + "\",\"quantidade\":" + quantidade + "}";
    }

    @Test void deveRegistrarEntradaSaidaEConsultarHistorico() throws Exception {
        long id = cadastrar();
        mvc.perform(post("/api/lancamentos-estoque").contentType(MediaType.APPLICATION_JSON).content(movimento(id,"ENTRADA","10")))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.saldoAnterior").value(0)).andExpect(jsonPath("$.saldoPosterior").value(10));
        mvc.perform(post("/api/lancamentos-estoque").contentType(MediaType.APPLICATION_JSON).content(movimento(id,"SAIDA","3")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.saldoPosterior").value(7));
        mvc.perform(get("/api/produtos/{id}",id)).andExpect(status().isOk()).andExpect(jsonPath("$.saldoEstoque").value(7));
        mvc.perform(get("/api/lancamentos-estoque").param("produtoId",Long.toString(id)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test void deveRecusarSaidaSemSaldoSemGravarLancamento() throws Exception {
        long id = cadastrar();
        mvc.perform(post("/api/lancamentos-estoque").contentType(MediaType.APPLICATION_JSON).content(movimento(id,"ENTRADA","5")))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/lancamentos-estoque").contentType(MediaType.APPLICATION_JSON).content(movimento(id,"SAIDA","6")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("Saldo de estoque insuficiente"));
        mvc.perform(get("/api/produtos/{id}",id)).andExpect(jsonPath("$.saldoEstoque").value(5));
        mvc.perform(get("/api/lancamentos-estoque").param("produtoId",Long.toString(id)))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test void deveRecusarQuantidadeZeroNegativaOuPrecisaoExcessiva() throws Exception {
        for (String valor : new String[]{"0","-1","0.0001","1000000000000000"}) {
            mvc.perform(post("/api/lancamentos-estoque").contentType(MediaType.APPLICATION_JSON).content(movimento(1,"ENTRADA",valor)))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.quantidade").exists());
        }
    }

    @Test void deveRecusarProdutoInexistente() throws Exception {
        mvc.perform(post("/api/lancamentos-estoque").contentType(MediaType.APPLICATION_JSON).content(movimento(Long.MAX_VALUE,"ENTRADA","1")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Produto não encontrado"));
    }

    @Test void devePermitirRetiradaExataComDecimais() throws Exception {
        long id = cadastrar();
        mvc.perform(post("/api/lancamentos-estoque").contentType(MediaType.APPLICATION_JSON).content(movimento(id,"ENTRADA","1.125")))
                .andExpect(status().isCreated());
        String resposta = mvc.perform(post("/api/lancamentos-estoque").contentType(MediaType.APPLICATION_JSON).content(movimento(id,"SAIDA","1.125")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.saldoPosterior").value(0))
                .andReturn().getResponse().getContentAsString();
        long lancamento = json.readTree(resposta).get("id").asLong();
        mvc.perform(get("/api/lancamentos-estoque/{id}",lancamento))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidade").value(1.125));
    }

    @Test void duasSaidasConcorrentesNaoPodemConsumirOMesmoSaldo() throws Exception {
        long id = cadastrar();
        mvc.perform(post("/api/lancamentos-estoque").contentType(MediaType.APPLICATION_JSON).content(movimento(id,"ENTRADA","10")))
                .andExpect(status().isCreated());
        var prontas = new java.util.concurrent.CountDownLatch(2);
        var iniciar = new java.util.concurrent.CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<Integer> saida = () -> {
                prontas.countDown();
                if (!iniciar.await(10, java.util.concurrent.TimeUnit.SECONDS)) throw new AssertionError("Tempo de espera excedido");
                return mvc.perform(post("/api/lancamentos-estoque").contentType(MediaType.APPLICATION_JSON).content(movimento(id,"SAIDA","7")))
                        .andReturn().getResponse().getStatus();
            };
            var primeira = executor.submit(saida);
            var segunda = executor.submit(saida);
            org.junit.jupiter.api.Assertions.assertTrue(prontas.await(10, java.util.concurrent.TimeUnit.SECONDS));
            iniciar.countDown();
            var resultados = new java.util.ArrayList<>(java.util.List.of(
                    primeira.get(20, java.util.concurrent.TimeUnit.SECONDS), segunda.get(20, java.util.concurrent.TimeUnit.SECONDS)));
            resultados.sort(Integer::compareTo);
            org.junit.jupiter.api.Assertions.assertEquals(java.util.List.of(201,409),resultados);
        }
        mvc.perform(get("/api/produtos/{id}",id)).andExpect(jsonPath("$.saldoEstoque").value(3));
        mvc.perform(get("/api/lancamentos-estoque").param("produtoId",Long.toString(id)))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test void deveRecusarSaldoNoCadastro() throws Exception {
        mvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON).content("""
                {"codigoBarras":"INVALIDO","descricao":"Teste","saldoEstoque":10,
                 "valorUnitario":1,"estoqueMinimo":0,"grupoId":1}
                """))
                .andExpect(status().isBadRequest());
    }

    @Test void deveRecusarCodigoRepetido() throws Exception {
        long id = cadastrar();
        String produto = mvc.perform(get("/api/produtos/{id}",id)).andReturn().getResponse().getContentAsString();
        var dados = json.readTree(produto);
        String body = """
                {"codigoBarras":"%s","descricao":"Duplicado","valorUnitario":1,"estoqueMinimo":0,"grupoId":%d}
                """.formatted(dados.get("codigoBarras").asText(),dados.get("grupoId").asLong());
        mvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test void deveRecusarTipoInvalidoEJsonMalformado() throws Exception {
        for (String body : new String[]{movimento(1,"AJUSTE","1"),"{"}) {
            mvc.perform(post("/api/lancamentos-estoque").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        }
    }
}

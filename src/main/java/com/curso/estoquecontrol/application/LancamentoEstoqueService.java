package com.curso.estoquecontrol.application;

import com.curso.estoquecontrol.domain.*;
import com.curso.estoquecontrol.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class LancamentoEstoqueService {
    private final ProdutoRepository produtos;
    private final LancamentoEstoqueRepository lancamentos;

    public LancamentoEstoqueService(ProdutoRepository produtos, LancamentoEstoqueRepository lancamentos) {
        this.produtos = produtos;
        this.lancamentos = lancamentos;
    }

    @Transactional
    public LancamentoEstoque registrar(Long produtoId, TipoMovimento tipo, BigDecimal quantidade, String observacao) {
        // O bloqueio dura até o commit: duas saídas não podem consumir o mesmo saldo.
        Produto produto = produtos.buscarParaMovimentar(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
        return lancamentos.save(new LancamentoEstoque(produto, tipo, quantidade, observacao));
    }

    @Transactional(readOnly = true)
    public LancamentoEstoque buscarPorId(Long id) {
        return lancamentos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lançamento não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<LancamentoEstoque> listar(Long produtoId) {
        if (produtoId == null) return lancamentos.findAllByOrderByIdAsc();
        if (!produtos.existsById(produtoId)) throw new RecursoNaoEncontradoException("Produto não encontrado");
        return lancamentos.findByProdutoIdOrderByIdAsc(produtoId);
    }
}

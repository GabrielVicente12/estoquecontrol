package com.curso.estoquecontrol.application;

import com.curso.estoquecontrol.domain.Fornecedor;
import com.curso.estoquecontrol.domain.GrupoProduto;
import com.curso.estoquecontrol.domain.Produto;
import com.curso.estoquecontrol.repository.FornecedorRepository;
import com.curso.estoquecontrol.repository.GrupoProdutoRepository;
import com.curso.estoquecontrol.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final GrupoProdutoRepository grupoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final LancamentoEstoqueService lancamentoService;

    public ProdutoService(
            ProdutoRepository produtoRepository,
            GrupoProdutoRepository grupoRepository,
            FornecedorRepository fornecedorRepository,
            LancamentoEstoqueService lancamentoService) {
        this.produtoRepository = produtoRepository;
        this.grupoRepository = grupoRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.lancamentoService = lancamentoService;
    }

    @Transactional
    public Produto cadastrar(Produto produto, Long grupoId, Long fornecedorId) {
        if (produto.getSaldoEstoque().signum() != 0) {
            throw new IllegalArgumentException("Produto deve iniciar com saldo zero; registre uma entrada");
        }
        if (produtoRepository.existsByCodigoBarras(produto.getCodigoBarras())) {
            throw new RecursoDuplicadoException("Código de barras já cadastrado");
        }

        GrupoProduto grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Grupo de produto não encontrado"));
        grupo.adicionarProduto(produto);

        if (fornecedorId != null) {
            Fornecedor fornecedor = fornecedorRepository.findById(fornecedorId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Fornecedor não encontrado"));
            produto.associarFornecedor(fornecedor);
        }

        return produtoRepository.save(produto);
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        return produtoRepository.buscarPorIdComRelacionamentos(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Produto> listar() {
        return produtoRepository.buscarTodosComRelacionamentos();
    }

    @Transactional
    public Produto receberEstoque(Long id, BigDecimal quantidade) {
        return lancamentoService.registrar(id, com.curso.estoquecontrol.domain.TipoMovimento.ENTRADA,
                quantidade, null).getProduto();
    }
}

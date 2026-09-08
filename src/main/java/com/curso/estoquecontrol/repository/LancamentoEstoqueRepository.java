package com.curso.estoquecontrol.repository;

import com.curso.estoquecontrol.domain.LancamentoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LancamentoEstoqueRepository extends JpaRepository<LancamentoEstoque, Long> {
    List<LancamentoEstoque> findAllByOrderByIdAsc();
    List<LancamentoEstoque> findByProdutoIdOrderByIdAsc(Long produtoId);
}

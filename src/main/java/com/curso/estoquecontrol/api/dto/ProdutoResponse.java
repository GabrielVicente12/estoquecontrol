package com.curso.estoquecontrol.api.dto;

import com.curso.estoquecontrol.domain.Status;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProdutoResponse(
        Long id,
        String codigoBarras,
        String descricao,
        BigDecimal saldoEstoque,
        BigDecimal valorUnitario,
        BigDecimal estoqueMinimo,
        BigDecimal valorEstoque,
        LocalDate dataCadastro,
        Status status,
        Long grupoId,
        String grupoNome,
        Long fornecedorId,
        String fornecedorRazaoSocial) {
}

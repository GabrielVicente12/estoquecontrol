package com.curso.estoquecontrol.api.mapper;

import com.curso.estoquecontrol.domain.LancamentoEstoque;
import com.curso.estoquecontrol.api.dto.LancamentoEstoqueResponse;
import org.springframework.stereotype.Component;

@Component
public class LancamentoEstoqueMapper {
    public LancamentoEstoqueResponse toResponse(LancamentoEstoque lancamento) {
        return new LancamentoEstoqueResponse(lancamento.getId(), lancamento.getProduto().getId(),
                lancamento.getTipo(), lancamento.getQuantidade(), lancamento.getSaldoAnterior(),
                lancamento.getSaldoPosterior(), lancamento.getDataLancamento(), lancamento.getObservacao());
    }
}

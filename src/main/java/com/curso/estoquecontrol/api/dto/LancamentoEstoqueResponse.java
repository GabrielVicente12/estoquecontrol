package com.curso.estoquecontrol.api.dto;

import com.curso.estoquecontrol.domain.TipoMovimento;
import java.math.BigDecimal;
import java.time.Instant;

public record LancamentoEstoqueResponse(Long id, Long produtoId, TipoMovimento tipo,
        BigDecimal quantidade, BigDecimal saldoAnterior, BigDecimal saldoPosterior,
        Instant dataLancamento, String observacao) {}

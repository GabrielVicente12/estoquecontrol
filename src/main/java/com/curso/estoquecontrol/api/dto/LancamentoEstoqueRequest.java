package com.curso.estoquecontrol.api.dto;

import com.curso.estoquecontrol.domain.TipoMovimento;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record LancamentoEstoqueRequest(
        @NotNull(message = "Produto é obrigatório") @Positive(message = "Produto deve ter identificador positivo") Long produtoId,
        @NotNull(message = "Tipo é obrigatório: ENTRADA ou SAIDA") TipoMovimento tipo,
        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        @Digits(integer = 15, fraction = 3, message = "Quantidade deve ter até 15 inteiros e 3 casas decimais") BigDecimal quantidade,
        @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres") String observacao) {}

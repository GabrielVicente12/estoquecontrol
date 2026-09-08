package com.curso.estoquecontrol.api.dto;

import com.curso.estoquecontrol.domain.Status;

public record GrupoProdutoResponse(
        Long id,
        String nome,
        Status status) {
}

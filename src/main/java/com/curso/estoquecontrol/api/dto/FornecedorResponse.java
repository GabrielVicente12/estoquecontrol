package com.curso.estoquecontrol.api.dto;

import com.curso.estoquecontrol.domain.Status;

public record FornecedorResponse(
        Long id,
        String razaoSocial,
        String cnpj,
        Status status) {
}

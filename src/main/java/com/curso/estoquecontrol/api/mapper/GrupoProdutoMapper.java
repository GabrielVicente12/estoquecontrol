package com.curso.estoquecontrol.api.mapper;

import com.curso.estoquecontrol.api.dto.GrupoProdutoResponse;
import com.curso.estoquecontrol.domain.GrupoProduto;
import org.springframework.stereotype.Component;

@Component
public class GrupoProdutoMapper {

    public GrupoProdutoResponse toResponse(GrupoProduto grupo) {
        return new GrupoProdutoResponse(
                grupo.getId(),
                grupo.getNome(),
                grupo.getStatus());
    }
}

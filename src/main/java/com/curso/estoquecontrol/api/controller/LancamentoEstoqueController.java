package com.curso.estoquecontrol.api.controller;

import com.curso.estoquecontrol.api.dto.*;
import com.curso.estoquecontrol.api.mapper.LancamentoEstoqueMapper;
import com.curso.estoquecontrol.application.LancamentoEstoqueService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/lancamentos-estoque")
public class LancamentoEstoqueController {
    private final LancamentoEstoqueService service;
    private final LancamentoEstoqueMapper mapper;

    public LancamentoEstoqueController(LancamentoEstoqueService service, LancamentoEstoqueMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<LancamentoEstoqueResponse> registrar(@Valid @RequestBody LancamentoEstoqueRequest request) {
        var lancamento = service.registrar(request.produtoId(), request.tipo(), request.quantidade(), request.observacao());
        return ResponseEntity.created(URI.create("/api/lancamentos-estoque/" + lancamento.getId()))
                .body(mapper.toResponse(lancamento));
    }

    @GetMapping("/{id}")
    public LancamentoEstoqueResponse buscarPorId(@PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @GetMapping
    public List<LancamentoEstoqueResponse> listar(@RequestParam(required = false) Long produtoId) {
        return service.listar(produtoId).stream().map(mapper::toResponse).toList();
    }
}

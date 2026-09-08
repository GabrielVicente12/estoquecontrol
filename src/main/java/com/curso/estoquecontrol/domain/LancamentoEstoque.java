package com.curso.estoquecontrol.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "lancamento_estoque")
public class LancamentoEstoque {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lancamento_produto"))
    private Produto produto;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimento tipo;
    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal quantidade;
    @Column(name = "saldo_anterior", nullable = false, precision = 18, scale = 3)
    private BigDecimal saldoAnterior;
    @Column(name = "saldo_posterior", nullable = false, precision = 18, scale = 3)
    private BigDecimal saldoPosterior;
    @Column(name = "data_lancamento", nullable = false)
    private Instant dataLancamento;
    @Column(length = 255)
    private String observacao;

    protected LancamentoEstoque() {}

    public LancamentoEstoque(Produto produto, TipoMovimento tipo, BigDecimal quantidade, String observacao) {
        this.produto = Objects.requireNonNull(produto, "Produto é obrigatório");
        this.tipo = Objects.requireNonNull(tipo, "Tipo é obrigatório");
        if (observacao != null && observacao.length() > 255) {
            throw new IllegalArgumentException("Observação deve ter no máximo 255 caracteres");
        }
        this.saldoAnterior = produto.getSaldoEstoque();
        if (tipo == TipoMovimento.ENTRADA) {
            produto.receberEstoque(quantidade);
        } else {
            produto.retirarEstoque(quantidade);
        }
        this.quantidade = quantidade;
        this.saldoPosterior = produto.getSaldoEstoque();
        this.dataLancamento = Instant.now();
        this.observacao = observacao == null ? null : observacao.trim();
    }

    public Long getId() { return id; }
    public Produto getProduto() { return produto; }
    public TipoMovimento getTipo() { return tipo; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getSaldoAnterior() { return saldoAnterior; }
    public BigDecimal getSaldoPosterior() { return saldoPosterior; }
    public Instant getDataLancamento() { return dataLancamento; }
    public String getObservacao() { return observacao; }
}

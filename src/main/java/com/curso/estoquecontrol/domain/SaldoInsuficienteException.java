package com.curso.estoquecontrol.domain;

public class SaldoInsuficienteException extends IllegalArgumentException {
    public SaldoInsuficienteException() {
        super("Saldo de estoque insuficiente");
    }
}

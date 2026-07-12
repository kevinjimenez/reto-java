package com.banco.ms_cuenta.exceptions;

public class SaldoNoDisponibleException extends RuntimeException {
    public SaldoNoDisponibleException() {
        super("Saldo no disponible");
    }

    public SaldoNoDisponibleException(String message) {
        super(message);
    }
}

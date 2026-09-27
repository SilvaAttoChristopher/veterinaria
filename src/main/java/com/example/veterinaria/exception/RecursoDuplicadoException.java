package com.example.veterinaria.exception;

/**
 * Conflicto de unicidad u otra colisión de negocio (usuario o dni ya existentes).
 * Se traduce a HTTP 409, a diferencia de {@link IllegalArgumentException} (400).
 */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}

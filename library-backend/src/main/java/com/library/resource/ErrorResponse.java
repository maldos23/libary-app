package com.library.resource;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Envoltorio estándar para respuestas de error.
 * Evita la construcción manual de cadenas JSON que puede llevar
 * a JSON-injection si el mensaje contiene caracteres especiales.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    public final String error;
    public final String field;

    public ErrorResponse(String error) {
        this.error = error;
        this.field = null;
    }

    public ErrorResponse(String error, String field) {
        this.error = error;
        this.field = field;
    }
}

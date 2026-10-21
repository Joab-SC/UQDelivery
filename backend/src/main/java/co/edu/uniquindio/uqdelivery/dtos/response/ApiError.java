package co.edu.uniquindio.uqdelivery.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Forma única de todas las respuestas de error de la API.
 * Ejemplo: { "status": 400, "message": "Hay campos inválidos", "errors": { "pesoKg": "Debe ser mayor a 0" } }
 *
 * @param status  código HTTP
 * @param message mensaje para mostrar al usuario
 * @param errors  error de cada campo; solo aparece en errores de validación
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(int status, String message, Map<String, String> errors) {

    public ApiError(int status, String message) {
        this(status, message, null);
    }
}

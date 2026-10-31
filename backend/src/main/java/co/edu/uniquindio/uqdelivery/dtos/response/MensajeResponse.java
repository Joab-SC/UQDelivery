package co.edu.uniquindio.uqdelivery.dtos.response;

/**
 * Respuesta con un solo mensaje para mostrar al usuario.
 * La usa HU-03 (recuperar y restablecer); sirve para cualquier acción que no devuelva datos.
 */
public record MensajeResponse(
        String mensaje
) {}

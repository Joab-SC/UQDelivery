package co.edu.uniquindio.uqdelivery.dtos.response;

/**
 * Respuesta de GET /api/health.
 *
 * @param estado siempre "UP" si la API responde
 */
public record HealthResponse(String estado) {
}

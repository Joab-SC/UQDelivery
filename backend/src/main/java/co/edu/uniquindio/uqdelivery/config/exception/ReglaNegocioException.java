package co.edu.uniquindio.uqdelivery.config.exception;

/**
 * Se incumple una regla del negocio (400). Ej.: "El paquete no cumple con las políticas de tamaño".
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}

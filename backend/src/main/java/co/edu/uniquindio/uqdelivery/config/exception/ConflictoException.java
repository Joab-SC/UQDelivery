package co.edu.uniquindio.uqdelivery.config.exception;

/**
 * Choca con datos que ya existen (409). Ej.: "El correo ya está registrado".
 */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}

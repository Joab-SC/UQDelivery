package co.edu.uniquindio.uqdelivery.config.exception;

/**
 * Lo que se busca no existe (404). Ej.: "Usuario no encontrado".
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

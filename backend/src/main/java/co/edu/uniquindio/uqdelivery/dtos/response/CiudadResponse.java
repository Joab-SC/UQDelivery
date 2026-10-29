package co.edu.uniquindio.uqdelivery.dtos.response;

/**
 * Ciudad para la lista del formulario (HU-04).
 *
 * @param codigo valor del enum que se envía al back, p. ej. "CALARCA"
 * @param nombre nombre para mostrar, p. ej. "Calarcá"
 */
public record CiudadResponse(String codigo, String nombre) {
}

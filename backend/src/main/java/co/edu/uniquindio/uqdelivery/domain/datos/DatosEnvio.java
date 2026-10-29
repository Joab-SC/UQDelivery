package co.edu.uniquindio.uqdelivery.domain.datos;

import co.edu.uniquindio.uqdelivery.domain.model.Ciudad;

import java.math.BigDecimal;

/**
 * Datos que el emisor escribe para crear un envío (HU-04). No es una tabla.
 * El mapper convierte el EnvioRequest en este record para que el service no reciba un DTO
 * ni doce parámetros sueltos. No trae emisor ni costo: el emisor sale del token y el costo
 * lo calcula siempre el back con la cotización de HU-05.
 */
public record DatosEnvio(
        String direccionOrigen,
        Ciudad ciudadOrigen,
        String direccionDestino,
        Ciudad ciudadDestino,
        String nombreDestinatario,
        String telefonoDestinatario,
        String correoDestinatario,
        String descripcion,
        BigDecimal pesoKg,
        BigDecimal largoCm,
        BigDecimal anchoCm,
        BigDecimal altoCm) {
}
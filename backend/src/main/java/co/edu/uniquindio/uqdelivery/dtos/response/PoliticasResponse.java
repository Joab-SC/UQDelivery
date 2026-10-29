package co.edu.uniquindio.uqdelivery.dtos.response;

import java.math.BigDecimal;

/**
 * Límites del paquete para validar el formulario en el front (HU-04).
 *
 * @param pesoMaximoKg   peso máximo permitido
 * @param medidaMaximaCm máximo para cada medida (largo, ancho y alto)
 */
public record PoliticasResponse(BigDecimal pesoMaximoKg, BigDecimal medidaMaximaCm) {
}

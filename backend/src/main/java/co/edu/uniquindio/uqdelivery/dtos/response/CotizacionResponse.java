package co.edu.uniquindio.uqdelivery.dtos.response;

import java.math.BigDecimal;

/**
 * Resultado de cotizar un envío.
 *
 * @param costo       costo en COP, redondeado a la centena
 * @param distanciaKm distancia estimada entre las ciudades
 */
public record CotizacionResponse(BigDecimal costo, BigDecimal distanciaKm) {
}
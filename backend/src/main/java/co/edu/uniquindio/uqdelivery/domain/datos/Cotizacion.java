package co.edu.uniquindio.uqdelivery.domain.datos;

import java.math.BigDecimal;

/**
 * Resultado de cotizar un envío (HU-05). No es una tabla: solo se calcula y se devuelve.
 * Lo devuelve CotizacionService; el controller lo convierte en CotizacionResponse y HU-04 lo usa
 * para guardar el costo y la distancia del envío.
 *
 * @param costo       costo en pesos colombianos (COP), redondeado a la centena
 * @param distanciaKm distancia estimada entre las ciudades, en kilómetros
 */
public record Cotizacion(BigDecimal costo, BigDecimal distanciaKm) {
}

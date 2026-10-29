package co.edu.uniquindio.uqdelivery.dtos.response;

import co.edu.uniquindio.uqdelivery.domain.model.Ciudad;
import co.edu.uniquindio.uqdelivery.domain.model.EstadoEnvio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Envío creado (HU-04). No incluye al emisor: es el mismo usuario que hizo la petición.
 */
public record EnvioResponse(
        Long id,
        String codigoSeguimiento,
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
        BigDecimal altoCm,
        BigDecimal distanciaKm,
        BigDecimal costo,
        EstadoEnvio estado,
        LocalDateTime fechaCreacion) {
}
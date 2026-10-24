package co.edu.uniquindio.uqdelivery.dtos.request;

import co.edu.uniquindio.uqdelivery.domain.model.Ciudad;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Datos para cotizar un envío (HU-05). */
public record CotizacionRequest(
        @NotNull(message = "La ciudad de origen es obligatoria") Ciudad ciudadOrigen,
        @NotNull(message = "La ciudad de destino es obligatoria") Ciudad ciudadDestino,
        @NotNull(message = "El peso es obligatorio") @Positive(message = "Debe ser mayor a 0") BigDecimal pesoKg,
        @NotNull(message = "El largo es obligatorio") @Positive(message = "Debe ser mayor a 0") BigDecimal largoCm,
        @NotNull(message = "El ancho es obligatorio") @Positive(message = "Debe ser mayor a 0") BigDecimal anchoCm,
        @NotNull(message = "El alto es obligatorio") @Positive(message = "Debe ser mayor a 0") BigDecimal altoCm) {
}
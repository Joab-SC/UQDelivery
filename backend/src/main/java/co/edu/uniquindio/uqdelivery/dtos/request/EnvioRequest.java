package co.edu.uniquindio.uqdelivery.dtos.request;

import co.edu.uniquindio.uqdelivery.domain.model.Ciudad;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Formulario de solicitud de envío (HU-04).
 * Sin emisorId (sale del token) y sin costo (lo recalcula el back).
 * Los límites de peso y medidas no se validan aquí: dependen de configuracion_tarifa
 * y los valida CotizacionService.
 */
public record EnvioRequest(
        @NotBlank(message = "La dirección de origen es obligatoria")
        @Size(max = 200, message = "Máximo 200 caracteres")
        String direccionOrigen,

        @NotNull(message = "La ciudad de origen es obligatoria")
        Ciudad ciudadOrigen,

        @NotBlank(message = "La dirección de destino es obligatoria")
        @Size(max = 200, message = "Máximo 200 caracteres")
        String direccionDestino,

        @NotNull(message = "La ciudad de destino es obligatoria")
        Ciudad ciudadDestino,

        @NotBlank(message = "El nombre del destinatario es obligatorio")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String nombreDestinatario,

        @NotBlank(message = "El teléfono del destinatario es obligatorio")
        @Pattern(regexp = "^[0-9]{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos")
        String telefonoDestinatario,

        /* Opcional: puede llegar null o vacío. */
        @Email(message = "El correo no es válido")
        @Size(max = 150, message = "Máximo 150 caracteres")
        String correoDestinatario,

        @NotBlank(message = "La descripción del paquete es obligatoria")
        @Size(max = 500, message = "Máximo 500 caracteres")
        String descripcion,

        @NotNull(message = "El peso es obligatorio")
        @Positive(message = "Debe ser mayor a 0")
        @Digits(integer = 6, fraction = 2, message = "Máximo 2 decimales")
        BigDecimal pesoKg,

        @NotNull(message = "El largo es obligatorio")
        @Positive(message = "Debe ser mayor a 0")
        @Digits(integer = 6, fraction = 2, message = "Máximo 2 decimales")
        BigDecimal largoCm,

        @NotNull(message = "El ancho es obligatorio")
        @Positive(message = "Debe ser mayor a 0")
        @Digits(integer = 6, fraction = 2, message = "Máximo 2 decimales")
        BigDecimal anchoCm,

        @NotNull(message = "El alto es obligatorio")
        @Positive(message = "Debe ser mayor a 0")
        @Digits(integer = 6, fraction = 2, message = "Máximo 2 decimales")
        BigDecimal altoCm) {
}
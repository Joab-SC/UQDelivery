package co.edu.uniquindio.uqdelivery.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** HU-03: lo que llega a POST /api/auth/recuperar. */
public record RecuperarRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El formato del correo no es válido")
        @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
        String correo
) {}

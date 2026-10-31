package co.edu.uniquindio.uqdelivery.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * HU-03: lo que llega a POST /api/auth/restablecer.
 * La contraseña nueva tiene las mismas reglas que en el registro (HU-01).
 */
public record RestablecerRequest(
        @NotBlank(message = "El enlace de recuperación no es válido")
        @Size(max = 36, message = "El enlace de recuperación no es válido")
        String token,

        @NotBlank(message = "La contraseña es obligatoria")
        // BCrypt solo tiene en cuenta los primeros 72 bytes: por eso el máximo es 72
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "La contraseña debe contener al menos una mayúscula, una minúscula y un número")
        String contrasena,

        @NotBlank(message = "La confirmación de la contraseña es obligatoria")
        @Size(max = 100, message = "La confirmación no puede superar los 100 caracteres")
        String confirmarContrasena
) {}

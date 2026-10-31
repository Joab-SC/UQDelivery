package co.edu.uniquindio.uqdelivery.dtos.request;

import co.edu.uniquindio.uqdelivery.domain.model.Rol;
import jakarta.validation.constraints.NotNull;

public record CambiarRolRequest(
        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {}

package co.edu.uniquindio.uqdelivery.dtos.response;

import co.edu.uniquindio.uqdelivery.domain.model.Rol;
import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        String telefono,
        Rol rol,
        LocalDateTime fechaRegistro
) {}
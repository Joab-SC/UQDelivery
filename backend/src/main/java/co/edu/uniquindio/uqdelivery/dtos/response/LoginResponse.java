package co.edu.uniquindio.uqdelivery.dtos.response;

public record LoginResponse(
        String token,
        UsuarioResponse usuario
) {}
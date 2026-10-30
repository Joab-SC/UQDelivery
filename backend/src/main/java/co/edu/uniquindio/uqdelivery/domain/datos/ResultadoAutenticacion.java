package co.edu.uniquindio.uqdelivery.domain.datos;

import co.edu.uniquindio.uqdelivery.domain.model.Usuario;

public record ResultadoAutenticacion(
        String token,
        Usuario usuario
) {}
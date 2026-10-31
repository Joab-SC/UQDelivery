package co.edu.uniquindio.uqdelivery.domain.datos;

/**
 * Evento de HU-03: se creó un enlace de recuperación y hay que enviarlo por correo.
 * Lo publica RecuperacionService y lo atiende NotificadorRecuperacion en segundo plano,
 * después de que la transacción se guarda.
 * Lleva solo textos (no la entidad Usuario) porque se usa en otro hilo, ya fuera de la transacción.
 */
public record EnlaceRecuperacionCreado(
        String correo,
        String nombre,
        String enlace
) {}

package co.edu.uniquindio.uqdelivery.config.security;

import co.edu.uniquindio.uqdelivery.config.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Da acceso al usuario que hizo la petición. Se inyecta en los services.
 * Ejemplo (HU-04): {@code Usuario emisor = usuarioAutenticado.obtener();}
 */
@Component
@RequiredArgsConstructor
public class UsuarioAutenticado {

    private final UsuarioRepository usuarioRepository;

    /** Correo del usuario autenticado, sin consultar la base de datos. */
    public String obtenerCorreo() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated() || autenticacion.getName() == null
                || "anonymousUser".equals(autenticacion.getName())) {
            throw new AuthenticationCredentialsNotFoundException("No hay un usuario autenticado");
        }
        return autenticacion.getName();
    }

    /** Entidad del usuario autenticado, cargada de la base de datos. */
    public Usuario obtener() {
        return usuarioRepository.findByCorreo(obtenerCorreo())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}

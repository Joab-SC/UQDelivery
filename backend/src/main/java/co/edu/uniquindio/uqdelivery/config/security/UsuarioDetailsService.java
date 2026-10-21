package co.edu.uniquindio.uqdelivery.config.security;

import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Le dice a Spring Security cómo cargar un usuario a partir de su correo.
 * Lo usan el filtro JWT y el AuthenticationManager del login (HU-01).
 * La autoridad es "ROLE_" + rol, necesaria para que funcione hasRole('ADMIN').
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.withUsername(usuario.getCorreo())
                .password(usuario.getContrasenaHash())
                .authorities("ROLE_" + usuario.getRol().name())
                .disabled(!usuario.isActivo())
                .build();
    }
}

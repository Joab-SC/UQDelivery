package co.edu.uniquindio.uqdelivery.config.security;

import co.edu.uniquindio.uqdelivery.config.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.domain.model.Rol;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.repositories.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioAutenticadoTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioAutenticado usuarioAutenticado;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String correo) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                correo, null, List.of(new SimpleGrantedAuthority("ROLE_EMISOR"))));
    }

    @Test
    void devuelveElUsuarioDeLaPeticion() {
        Usuario emisor = new Usuario("Emisor", "emisor@uqdelivery.co", "hash", "300", Rol.EMISOR);
        autenticarComo("emisor@uqdelivery.co");
        when(usuarioRepository.findByCorreo("emisor@uqdelivery.co")).thenReturn(Optional.of(emisor));

        assertThat(usuarioAutenticado.obtener()).isSameAs(emisor);
    }

    @Test
    void fallaSiNoHayUsuarioAutenticado() {
        assertThatThrownBy(() -> usuarioAutenticado.obtenerCorreo())
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void fallaSiElUsuarioDelTokenYaNoExiste() {
        autenticarComo("borrado@correo.co");
        when(usuarioRepository.findByCorreo("borrado@correo.co")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioAutenticado.obtener()).isInstanceOf(RecursoNoEncontradoException.class);
    }
}

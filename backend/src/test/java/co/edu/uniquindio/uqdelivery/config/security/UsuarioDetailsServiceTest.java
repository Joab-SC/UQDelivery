package co.edu.uniquindio.uqdelivery.config.security;

import co.edu.uniquindio.uqdelivery.domain.model.Rol;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void cargaElUsuarioConSuRolComoAutoridad() {
        Usuario admin = new Usuario("Admin", "admin@uqdelivery.co", "hash", "300", Rol.ADMIN);
        when(usuarioRepository.findByCorreo("admin@uqdelivery.co")).thenReturn(Optional.of(admin));

        UserDetails detalles = usuarioDetailsService.loadUserByUsername("  Admin@UQDelivery.co ");

        assertThat(detalles.getUsername()).isEqualTo("admin@uqdelivery.co");
        assertThat(detalles.getPassword()).isEqualTo("hash");
        assertThat(detalles.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
        assertThat(detalles.isEnabled()).isTrue();
    }

    @Test
    void fallaSiElCorreoNoExiste() {
        when(usuarioRepository.findByCorreo("nadie@correo.co")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioDetailsService.loadUserByUsername("nadie@correo.co"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}

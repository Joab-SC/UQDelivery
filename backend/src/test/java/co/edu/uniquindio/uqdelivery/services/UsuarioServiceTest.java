package co.edu.uniquindio.uqdelivery.services;

import co.edu.uniquindio.uqdelivery.config.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.config.exception.ReglaNegocioException;
import co.edu.uniquindio.uqdelivery.config.security.UsuarioAutenticado;
import co.edu.uniquindio.uqdelivery.domain.model.Rol;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioAutenticado usuarioAutenticado;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioConId(Long id, Rol rol) {
        Usuario usuario = mock(Usuario.class);
        lenient().when(usuario.getId()).thenReturn(id);
        lenient().when(usuario.getRol()).thenReturn(rol);
        return usuario;
    }

    @Test
    void listarDevuelveLosUsuariosOrdenadosPorNombre() {
        List<Usuario> usuarios = List.of(mock(Usuario.class), mock(Usuario.class));
        when(usuarioRepository.findAll(any(Sort.class))).thenReturn(usuarios);

        assertThat(usuarioService.listar()).isSameAs(usuarios);
        verify(usuarioRepository).findAll(Sort.by("nombre"));
    }

    @Test
    void cambiarRolActualizaYGuardaElUsuario() {
        Usuario admin = usuarioConId(1L, Rol.ADMIN);
        Usuario objetivo = usuarioConId(2L, Rol.EMISOR);
        when(usuarioAutenticado.obtener()).thenReturn(admin);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(objetivo));
        when(usuarioRepository.save(objetivo)).thenReturn(objetivo);

        Usuario resultado = usuarioService.cambiarRol(2L, Rol.REPARTIDOR);

        assertThat(resultado).isSameAs(objetivo);
        verify(objetivo).cambiarRol(Rol.REPARTIDOR);
        verify(usuarioRepository).save(objetivo);
    }

    @Test
    void elAdminNoPuedeCambiarSuPropioRol() {
        Usuario admin = usuarioConId(1L, Rol.ADMIN);
        when(usuarioAutenticado.obtener()).thenReturn(admin);

        assertThatThrownBy(() -> usuarioService.cambiarRol(1L, Rol.EMISOR))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("No puedes cambiar tu propio rol");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarRolDeUsuarioInexistenteLanza404() {
        Usuario admin = usuarioConId(1L, Rol.ADMIN);
        when(usuarioAutenticado.obtener()).thenReturn(admin);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.cambiarRol(99L, Rol.EMISOR))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Usuario no encontrado");
        verify(usuarioRepository, never()).save(any());
    }
}

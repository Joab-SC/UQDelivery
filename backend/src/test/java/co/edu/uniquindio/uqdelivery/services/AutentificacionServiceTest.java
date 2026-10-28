package co.edu.uniquindio.uqdelivery.services;

import co.edu.uniquindio.uqdelivery.config.exception.ConflictoException;
import co.edu.uniquindio.uqdelivery.config.exception.ReglaNegocioException;
import co.edu.uniquindio.uqdelivery.config.security.JwtService;
import co.edu.uniquindio.uqdelivery.domain.datos.ResultadoAutenticacion;
import co.edu.uniquindio.uqdelivery.domain.model.Rol;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutentificacionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AutenticacionService authService;

    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new Usuario(
                "Juan Perez",
                "juan@uqdelivery.co",
                "hashPassword",
                "3001234567",
                Rol.EMISOR
        );
    }

    @Test
    void registrar_Exitoso() {
        when(usuarioRepository.existsByCorreo("juan@uqdelivery.co")).thenReturn(false);
        when(passwordEncoder.encode("Clave123*")).thenReturn("hashPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioPrueba);

        Usuario resultado = authService.registrar(
                "Juan Perez",
                "JUAN@UQDELIVERY.CO",
                "3001234567",
                "Clave123*",
                "Clave123*"
        );

        assertNotNull(resultado);
        assertEquals("juan@uqdelivery.co", resultado.getCorreo());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void registrar_ContrasenasNoCoinciden_LanzaExcepcion() {
        assertThrows(ReglaNegocioException.class, () -> {
            authService.registrar(
                    "Juan Perez",
                    "juan@uqdelivery.co",
                    "3001234567",
                    "Clave123*",
                    "OtraClave123*"
            );
        });
    }

    @Test
    void registrar_CorreoDuplicado_LanzaExcepcion() {
        when(usuarioRepository.existsByCorreo("juan@uqdelivery.co")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> {
            authService.registrar(
                    "Juan Perez",
                    "juan@uqdelivery.co",
                    "3001234567",
                    "Clave123*",
                    "Clave123*"
            );
        });
    }

    @Test
    void login_Exitoso() {
        when(usuarioRepository.findByCorreo("juan@uqdelivery.co")).thenReturn(Optional.of(usuarioPrueba));
        when(jwtService.generarToken(usuarioPrueba)).thenReturn("jwt-mock-token");

        ResultadoAutenticacion resultado = authService.login("juan@uqdelivery.co", "Clave123*");

        assertNotNull(resultado);
        assertEquals("jwt-mock-token", resultado.token());
        assertEquals(usuarioPrueba, resultado.usuario());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
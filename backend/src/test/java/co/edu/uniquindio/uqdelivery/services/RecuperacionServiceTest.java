package co.edu.uniquindio.uqdelivery.services;

import co.edu.uniquindio.uqdelivery.config.exception.ReglaNegocioException;
import co.edu.uniquindio.uqdelivery.domain.model.Rol;
import co.edu.uniquindio.uqdelivery.domain.model.TokenRecuperacion;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.repositories.TokenRecuperacionRepository;
import co.edu.uniquindio.uqdelivery.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas de HU-03. Cubren los dos criterios de la historia:
 * 1. Se pide el enlace con un correo registrado → se crea un token de 30 minutos y se envía por correo.
 * 2. Se usa un enlace vencido o ya usado → se rechaza y se pide uno nuevo.
 * El correo se simula con Mockito: no se envía nada de verdad.
 */
@ExtendWith(MockitoExtension.class)
class RecuperacionServiceTest {

    private static final String FRONT = "http://localhost:4200";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TokenRecuperacionRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CorreoService correoService;

    private RecuperacionService recuperacionService;

    private Usuario usuario;

    @BeforeEach
    void preparar() {
        recuperacionService = new RecuperacionService(
                usuarioRepository, tokenRepository, passwordEncoder, correoService, FRONT);
        usuario = new Usuario("Emisor de prueba", "emisor@uqdelivery.co", "hash-viejo", "3000000002", Rol.EMISOR);
    }

    // ---------------------------------------------------------------- solicitar

    @Test
    void conCorreoRegistradoCreaUnTokenDe30MinutosYEnviaElEnlaceAlFront() {
        when(usuarioRepository.findByCorreo("emisor@uqdelivery.co")).thenReturn(Optional.of(usuario));

        recuperacionService.solicitar("emisor@uqdelivery.co");

        ArgumentCaptor<TokenRecuperacion> tokenGuardado = ArgumentCaptor.forClass(TokenRecuperacion.class);
        verify(tokenRepository).save(tokenGuardado.capture());
        TokenRecuperacion token = tokenGuardado.getValue();

        assertThat(token.getUsuario()).isSameAs(usuario);
        assertThat(token.isUsado()).isFalse();
        assertThat(UUID.fromString(token.getToken())).isNotNull(); // es un UUID válido
        assertThat(token.getFechaExpiracion())
                .isCloseTo(LocalDateTime.now().plusMinutes(30), within(5, SECONDS));

        verify(correoService).enviar(
                eq("emisor@uqdelivery.co"),
                anyString(),
                contains(FRONT + "/restablecer?token=" + token.getToken()));
    }

    @Test
    void normalizaElCorreoAntesDeBuscarlo() {
        when(usuarioRepository.findByCorreo("emisor@uqdelivery.co")).thenReturn(Optional.of(usuario));

        recuperacionService.solicitar("  Emisor@UQDelivery.co ");

        verify(usuarioRepository).findByCorreo("emisor@uqdelivery.co");
    }

    @Test
    void pedirUnEnlaceNuevoInvalidaLosAnterioresAntesDeCrearlo() {
        when(usuarioRepository.findByCorreo("emisor@uqdelivery.co")).thenReturn(Optional.of(usuario));

        recuperacionService.solicitar("emisor@uqdelivery.co");

        var orden = inOrder(tokenRepository);
        orden.verify(tokenRepository).invalidarPendientes(usuario);
        orden.verify(tokenRepository).save(any(TokenRecuperacion.class));
    }

    @Test
    void conCorreoNoRegistradoNoCreaTokenNiEnviaCorreoNiFalla() {
        when(usuarioRepository.findByCorreo("nadie@correo.co")).thenReturn(Optional.empty());

        recuperacionService.solicitar("nadie@correo.co");

        verifyNoInteractions(tokenRepository, correoService);
    }

    @Test
    void elNombreDelUsuarioSeEscapaEnElCorreo() {
        Usuario conHtml = new Usuario("<b>Ana</b>", "ana@correo.co", "hash", "300", Rol.EMISOR);
        when(usuarioRepository.findByCorreo("ana@correo.co")).thenReturn(Optional.of(conHtml));

        recuperacionService.solicitar("ana@correo.co");

        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(correoService).enviar(eq("ana@correo.co"), anyString(), cuerpo.capture());
        assertThat(cuerpo.getValue()).contains("&lt;b&gt;Ana&lt;/b&gt;").doesNotContain("<b>Ana</b>");
    }

    // ---------------------------------------------------------------- restablecer

    private TokenRecuperacion tokenQueVence(LocalDateTime fechaExpiracion) {
        return new TokenRecuperacion(usuario, "token-123", fechaExpiracion);
    }

    @Test
    void conEnlaceVigenteCambiaLaContrasenaYMarcaElEnlaceComoUsado() {
        TokenRecuperacion token = tokenQueVence(LocalDateTime.now().plusMinutes(10));
        when(tokenRepository.findByToken("token-123")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NuevaClave2026")).thenReturn("hash-nuevo");

        recuperacionService.restablecer("token-123", "NuevaClave2026", "NuevaClave2026");

        assertThat(usuario.getContrasenaHash()).isEqualTo("hash-nuevo");
        assertThat(token.isUsado()).isTrue();
        verify(usuarioRepository).save(usuario);
        verify(tokenRepository).save(token);
    }

    @Test
    void conEnlaceVencidoRechazaYPideUnoNuevo() {
        TokenRecuperacion token = tokenQueVence(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByToken("token-123")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> recuperacionService.restablecer("token-123", "NuevaClave2026", "NuevaClave2026"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage(RecuperacionService.MENSAJE_ENLACE_VENCIDO);

        assertThat(usuario.getContrasenaHash()).isEqualTo("hash-viejo");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void conEnlaceYaUsadoRechazaYPideUnoNuevo() {
        TokenRecuperacion token = tokenQueVence(LocalDateTime.now().plusMinutes(10));
        token.marcarUsado();
        when(tokenRepository.findByToken("token-123")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> recuperacionService.restablecer("token-123", "NuevaClave2026", "NuevaClave2026"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage(RecuperacionService.MENSAJE_ENLACE_VENCIDO);

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void conEnlaceQueNoExisteRechaza() {
        when(tokenRepository.findByToken("inventado")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recuperacionService.restablecer("inventado", "NuevaClave2026", "NuevaClave2026"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage(RecuperacionService.MENSAJE_ENLACE_INVALIDO);
    }

    @Test
    void siLasContrasenasNoCoincidenRechazaSinBuscarElEnlace() {
        assertThatThrownBy(() -> recuperacionService.restablecer("token-123", "NuevaClave2026", "OtraClave2026"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("Las contraseñas no coinciden");

        verifyNoInteractions(tokenRepository, passwordEncoder);
    }
}

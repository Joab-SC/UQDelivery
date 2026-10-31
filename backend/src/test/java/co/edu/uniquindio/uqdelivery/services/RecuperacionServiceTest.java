package co.edu.uniquindio.uqdelivery.services;

import co.edu.uniquindio.uqdelivery.config.exception.ReglaNegocioException;
import co.edu.uniquindio.uqdelivery.domain.datos.EnlaceRecuperacionCreado;
import co.edu.uniquindio.uqdelivery.domain.model.Rol;
import co.edu.uniquindio.uqdelivery.domain.model.TokenRecuperacion;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.repositories.TokenRecuperacionRepository;
import co.edu.uniquindio.uqdelivery.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas de HU-03. Cubren los dos criterios de la historia:
 * 1. Se pide el enlace con un correo registrado → se crea un token de 30 minutos y se publica el evento
 *    para enviar el correo (el envío real lo prueba NotificadorRecuperacionTest).
 * 2. Se usa un enlace vencido o ya usado → se rechaza y se pide uno nuevo.
 * Además: límite de frecuencia y límite de 72 bytes de BCrypt.
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
    private ApplicationEventPublisher eventos;

    private RecuperacionService recuperacionService;

    private Usuario usuario;

    @BeforeEach
    void preparar() {
        recuperacionService = new RecuperacionService(
                usuarioRepository, tokenRepository, passwordEncoder, eventos, FRONT);
        usuario = new Usuario("Emisor de prueba", "emisor@uqdelivery.co", "hash-viejo", "3000000002", Rol.EMISOR);
    }

    // ---------------------------------------------------------------- solicitar

    @Test
    void conCorreoRegistradoCreaUnTokenDe30MinutosYPublicaElEnlaceAlFront() {
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

        verify(eventos).publishEvent(new EnlaceRecuperacionCreado(
                "emisor@uqdelivery.co", "Emisor de prueba", FRONT + "/restablecer?token=" + token.getToken()));
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

        InOrder orden = inOrder(tokenRepository);
        orden.verify(tokenRepository).invalidarPendientes(usuario);
        orden.verify(tokenRepository).save(any(TokenRecuperacion.class));
    }

    @Test
    void conCorreoNoRegistradoNoCreaTokenNiPublicaNadaNiFalla() {
        when(usuarioRepository.findByCorreo("nadie@correo.co")).thenReturn(Optional.empty());

        recuperacionService.solicitar("nadie@correo.co");

        verifyNoInteractions(tokenRepository, eventos);
    }

    @Test
    void siPidioUnEnlaceHaceMenosDeUnMinutoNoCreaOtroNiEnviaCorreo() {
        when(usuarioRepository.findByCorreo("emisor@uqdelivery.co")).thenReturn(Optional.of(usuario));
        when(tokenRepository.existsByUsuarioAndFechaCreacionAfter(eq(usuario), any(LocalDateTime.class)))
                .thenReturn(true);

        recuperacionService.solicitar("emisor@uqdelivery.co");

        verify(tokenRepository, never()).invalidarPendientes(any());
        verify(tokenRepository, never()).save(any());
        verifyNoInteractions(eventos);
    }

    @Test
    void elLimiteDeFrecuenciaMiraElUltimoMinuto() {
        when(usuarioRepository.findByCorreo("emisor@uqdelivery.co")).thenReturn(Optional.of(usuario));

        recuperacionService.solicitar("emisor@uqdelivery.co");

        ArgumentCaptor<LocalDateTime> desde = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(tokenRepository).existsByUsuarioAndFechaCreacionAfter(eq(usuario), desde.capture());
        assertThat(desde.getValue()).isCloseTo(LocalDateTime.now().minusSeconds(60), within(5, SECONDS));
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

    @Test
    void rechazaContrasenasDeMasDe72BytesAunqueTenganMenosDe72Caracteres() {
        // 40 "ñ" son 40 caracteres pero 80 bytes en UTF-8: BCrypt ignoraría el final
        String larga = "Aa1" + "ñ".repeat(40);

        assertThatThrownBy(() -> recuperacionService.restablecer("token-123", larga, larga))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage(RecuperacionService.MENSAJE_CONTRASENA_LARGA);

        verifyNoInteractions(tokenRepository, passwordEncoder);
    }
}

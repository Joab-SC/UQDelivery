package co.edu.uniquindio.uqdelivery.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Métodos de dominio de HU-03 en TokenRecuperacion y Usuario. */
class TokenRecuperacionTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 30, 10, 0);

    private final Usuario usuario = new Usuario("Ana", "ana@correo.co", "hash", "300", Rol.EMISOR);

    @Test
    void estaVigenteAntesDeVencerYSinUsar() {
        TokenRecuperacion token = new TokenRecuperacion(usuario, "t", AHORA.plusMinutes(30));

        assertThat(token.estaVigente(AHORA)).isTrue();
        assertThat(token.estaVigente(AHORA.plusMinutes(29))).isTrue();
    }

    @Test
    void noEstaVigenteDesdeLaHoraDeVencimiento() {
        TokenRecuperacion token = new TokenRecuperacion(usuario, "t", AHORA.plusMinutes(30));

        assertThat(token.estaVigente(AHORA.plusMinutes(30))).isFalse();
        assertThat(token.estaVigente(AHORA.plusHours(1))).isFalse();
    }

    @Test
    void noEstaVigenteDespuesDeUsarse() {
        TokenRecuperacion token = new TokenRecuperacion(usuario, "t", AHORA.plusMinutes(30));

        token.marcarUsado();

        assertThat(token.isUsado()).isTrue();
        assertThat(token.estaVigente(AHORA)).isFalse();
    }

    @Test
    void elUsuarioCambiaSuContrasenaPorElNuevoHash() {
        usuario.cambiarContrasena("hash-nuevo");

        assertThat(usuario.getContrasenaHash()).isEqualTo("hash-nuevo");
    }

    @Test
    void elUsuarioNoAceptaUnHashVacio() {
        assertThatThrownBy(() -> usuario.cambiarContrasena(" "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(usuario.getContrasenaHash()).isEqualTo("hash");
    }
}

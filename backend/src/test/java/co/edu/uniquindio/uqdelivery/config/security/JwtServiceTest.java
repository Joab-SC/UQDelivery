package co.edu.uniquindio.uqdelivery.config.security;

import co.edu.uniquindio.uqdelivery.domain.model.Rol;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRETO = "clave-de-pruebas-con-mas-de-32-caracteres";

    private final Usuario usuario =
            new Usuario("Ana", "ana@correo.co", "hash", "3001234567", Rol.EMISOR);

    @Test
    void generaUnTokenDelQueSePuedeLeerElCorreo() {
        JwtService jwtService = new JwtService(SECRETO, 120);

        String token = jwtService.generarToken(usuario);

        assertThat(jwtService.extraerCorreo(token)).isEqualTo("ana@correo.co");
    }

    @Test
    void rechazaUnTokenVencido() {
        JwtService jwtService = new JwtService(SECRETO, -1);

        String token = jwtService.generarToken(usuario);

        assertThatThrownBy(() -> jwtService.extraerCorreo(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() {
        String token = new JwtService("otra-clave-distinta-con-mas-de-32-caracteres", 120).generarToken(usuario);

        assertThatThrownBy(() -> new JwtService(SECRETO, 120).extraerCorreo(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rechazaUnTokenMalFormado() {
        assertThatThrownBy(() -> new JwtService(SECRETO, 120).extraerCorreo("no-es-un-token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void exigeUnaClaveDeAlMenos32Caracteres() {
        assertThatThrownBy(() -> new JwtService("corta", 120))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }
}

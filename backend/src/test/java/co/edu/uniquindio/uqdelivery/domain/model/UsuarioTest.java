package co.edu.uniquindio.uqdelivery.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioTest {

    private Usuario nuevoUsuario() {
        return new Usuario("Ana", "ana@uqdelivery.co", "hash", "3001112233", Rol.EMISOR);
    }

    @Test
    void cambiarRolActualizaElRol() {
        Usuario usuario = nuevoUsuario();

        usuario.cambiarRol(Rol.REPARTIDOR);

        assertThat(usuario.getRol()).isEqualTo(Rol.REPARTIDOR);
    }

    @Test
    void cambiarRolConNuloLanzaError() {
        Usuario usuario = nuevoUsuario();

        assertThatThrownBy(() -> usuario.cambiarRol(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(usuario.getRol()).isEqualTo(Rol.EMISOR);
    }
}

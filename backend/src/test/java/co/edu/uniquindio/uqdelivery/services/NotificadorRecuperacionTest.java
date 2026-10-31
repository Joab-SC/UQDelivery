package co.edu.uniquindio.uqdelivery.services;

import co.edu.uniquindio.uqdelivery.domain.datos.EnlaceRecuperacionCreado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/** Pruebas del envío del correo de recuperación (HU-03). El correo se simula con Mockito. */
@ExtendWith(MockitoExtension.class)
class NotificadorRecuperacionTest {

    @Mock
    private CorreoService correoService;

    @InjectMocks
    private NotificadorRecuperacion notificador;

    @Test
    void enviaElCorreoConElEnlaceAlUsuario() {
        String enlace = "http://localhost:4200/restablecer?token=abc-123";

        notificador.alCrearseEnlace(new EnlaceRecuperacionCreado("emisor@uqdelivery.co", "Emisor", enlace));

        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(correoService).enviar(eq("emisor@uqdelivery.co"), eq(NotificadorRecuperacion.ASUNTO), cuerpo.capture());
        assertThat(cuerpo.getValue()).contains(enlace).contains("30 minutos").contains("Hola, Emisor");
    }

    @Test
    void elNombreDelUsuarioSeEscapaEnElCorreo() {
        String cuerpo = notificador.cuerpoCorreo("<b>Ana</b>", "http://localhost:4200/restablecer?token=x");

        assertThat(cuerpo).contains("&lt;b&gt;Ana&lt;/b&gt;").doesNotContain("<b>Ana</b>");
    }
}

package co.edu.uniquindio.uqdelivery.services;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Pruebas del envío de correos. JavaMailSender se simula: no sale ningún correo de verdad. */
@ExtendWith(MockitoExtension.class)
class CorreoServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private MimeMessage mensajeVacio() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    @Test
    void conBrevoConfiguradoEnviaElCorreoConRemitenteDestinatarioYAsunto() throws Exception {
        CorreoService correoService = new CorreoService(mailSender, "no-reply@uqdelivery.co", "usuario@brevo", false);
        MimeMessage mensaje = mensajeVacio();
        when(mailSender.createMimeMessage()).thenReturn(mensaje);

        boolean enviado = correoService.enviar("ana@correo.co", "Asunto", "<p>Hola</p>");

        assertThat(enviado).isTrue();
        verify(mailSender).send(mensaje);
        assertThat(mensaje.getFrom()[0].toString()).isEqualTo("no-reply@uqdelivery.co");
        assertThat(mensaje.getAllRecipients()[0].toString()).isEqualTo("ana@correo.co");
        assertThat(mensaje.getSubject()).isEqualTo("Asunto");
    }

    @Test
    void enModoConsolaSinCredencialesNoIntentaEnviarYDevuelveTrue() {
        CorreoService correoService = new CorreoService(mailSender, "no-reply@uqdelivery.co", "", true);

        boolean enviado = correoService.enviar("ana@correo.co", "Asunto", "<p>Hola</p>");

        assertThat(enviado).isTrue();
        verifyNoInteractions(mailSender);
    }

    @Test
    void sinCredencialesYSinModoConsolaNoEnviaYDevuelveFalse() {
        // Caso producción con MAIL_USER olvidado: no se envía, no se escribe el contenido en el log y se avisa con false
        CorreoService correoService = new CorreoService(mailSender, "no-reply@uqdelivery.co", "", false);

        boolean enviado = correoService.enviar("ana@correo.co", "Asunto", "<p>Hola</p>");

        assertThat(enviado).isFalse();
        verifyNoInteractions(mailSender);
    }

    @Test
    void siBrevoFallaNoLanzaExcepcionYDevuelveFalse() {
        CorreoService correoService = new CorreoService(mailSender, "no-reply@uqdelivery.co", "usuario@brevo", false);
        when(mailSender.createMimeMessage()).thenReturn(mensajeVacio());
        doThrow(new MailSendException("SMTP caído")).when(mailSender).send(any(MimeMessage.class));

        boolean enviado = correoService.enviar("ana@correo.co", "Asunto", "<p>Hola</p>");

        assertThat(enviado).isFalse();
    }
}

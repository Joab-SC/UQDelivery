package co.edu.uniquindio.uqdelivery.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Envía correos con Brevo (SMTP, puerto 2525). La configuración está en application.yml (spring.mail.*)
 * y las credenciales en el .env (MAIL_USER, MAIL_PASSWORD, MAIL_FROM).
 *
 * Dos decisiones importantes:
 * - Si el correo NO está configurado (MAIL_USER vacío, lo normal en local), no intenta enviarlo:
 *   escribe el contenido en la consola del back para poder probar el flujo sin una cuenta de Brevo.
 * - Si el envío falla, NO lanza excepción: lo registra en el log y devuelve false. Así quien lo llama
 *   decide qué hacer y un fallo del correo nunca le revela a nadie si un correo tiene cuenta (HU-03).
 */

@Slf4j
@Service
public class CorreoService {

    private final JavaMailSender mailSender;
    private final String remitente;
    private final String usuarioSmtp;

    public CorreoService(JavaMailSender mailSender,
                         @Value("${app.mail.from}") String remitente,
                         @Value("${spring.mail.username:}") String usuarioSmtp) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.usuarioSmtp = usuarioSmtp;
    }

    /**
     * Envía un correo con cuerpo HTML.
     *
     * @return true si se envió (o se mostró en consola por no estar configurado); false si falló.
     */

    public boolean enviar(String destinatario, String asunto, String cuerpoHtml) {
        if (!estaConfigurado()) {
            log.warn("""

                    ===== Correo NO enviado: MAIL_USER está vacío en el .env (modo local) =====
                    Para: {}
                    Asunto: {}
                    {}
                    ==========================================================================""",
                    destinatario, asunto, cuerpoHtml);
            return true;
        }

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, StandardCharsets.UTF_8.name());
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpoHtml, true);
            mailSender.send(mensaje);
            log.info("Correo enviado a {} con asunto '{}'", destinatario, asunto);
            return true;
        } catch (MailException | MessagingException ex) {
            log.error("No se pudo enviar el correo a {}: {}", destinatario, ex.getMessage());
            return false;
        }
    }

    private boolean estaConfigurado() {
        return usuarioSmtp != null && !usuarioSmtp.isBlank();
    }
}

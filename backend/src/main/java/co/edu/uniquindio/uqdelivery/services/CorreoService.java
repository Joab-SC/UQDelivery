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
 * Lo crea HU-03 y lo reutilizarán las notificaciones del Sprint 2 (HU-12, HU-13).
 *
 * Qué pasa según la configuración:
 * - MAIL_USER lleno → envía el correo de verdad.
 * - MAIL_USER vacío y MAIL_MODO_CONSOLA=true (solo en local) → no envía: escribe el correo en la consola
 *   para poder probar sin cuenta de Brevo.
 * - MAIL_USER vacío y modo consola apagado (producción) → no envía, registra un error SIN el contenido
 *   (los correos llevan enlaces con tokens, que nunca deben quedar en los logs) y devuelve false.
 *
 * Nunca lanza excepción: si el envío falla lo registra y devuelve false, para que un fallo del correo
 * no cambie la respuesta de la API.
 */
@Slf4j
@Service
public class CorreoService {

    private final JavaMailSender mailSender;
    private final String remitente;
    private final String usuarioSmtp;
    private final boolean modoConsola;

    public CorreoService(JavaMailSender mailSender,
                         @Value("${app.mail.from}") String remitente,
                         @Value("${spring.mail.username:}") String usuarioSmtp,
                         @Value("${app.mail.modo-consola:false}") boolean modoConsola) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.usuarioSmtp = usuarioSmtp;
        this.modoConsola = modoConsola;
    }

    /**
     * Envía un correo con cuerpo HTML.
     *
     * @return true si se envió (o se mostró en consola en modo local); false si no se pudo enviar.
     */
    public boolean enviar(String destinatario, String asunto, String cuerpoHtml) {
        if (!estaConfigurado()) {
            if (modoConsola) {
                log.warn("""

                        ===== Correo NO enviado: modo consola (solo local, MAIL_USER vacío) =====
                        Para: {}
                        Asunto: {}
                        {}
                        ==========================================================================""",
                        destinatario, asunto, cuerpoHtml);
                return true;
            }
            log.error("Correo no enviado a {}: MAIL_USER no está configurado", destinatario);
            return false;
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

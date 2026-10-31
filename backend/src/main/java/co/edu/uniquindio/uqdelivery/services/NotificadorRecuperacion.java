package co.edu.uniquindio.uqdelivery.services;

import co.edu.uniquindio.uqdelivery.domain.datos.EnlaceRecuperacionCreado;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

/**
 * Envía el correo de recuperación (HU-03) en segundo plano.
 *
 * Por qué no lo envía RecuperacionService directamente: si el correo se enviara dentro de la petición,
 * la API tardaría mucho más cuando el correo SÍ tiene cuenta (hay que esperar a Brevo) que cuando no la
 * tiene. Midiendo ese tiempo, alguien podría averiguar qué correos están registrados.
 *
 * - @TransactionalEventListener(AFTER_COMMIT): se ejecuta solo cuando el token ya quedó guardado.
 *   Si la transacción falla, no se envía un enlace que no existe.
 * - @Async: corre en otro hilo; la API responde sin esperar a Brevo y sin retener la conexión a la base.
 */
@Component
@RequiredArgsConstructor
public class NotificadorRecuperacion {

    static final String ASUNTO = "Recupera tu contraseña de UQDelivery";

    private final CorreoService correoService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCrearseEnlace(EnlaceRecuperacionCreado evento) {
        correoService.enviar(evento.correo(), ASUNTO, cuerpoCorreo(evento.nombre(), evento.enlace()));
    }

    /** HTML del correo. El nombre se escapa para que no se pueda inyectar HTML en el correo. */
    String cuerpoCorreo(String nombre, String enlace) {
        String nombreSeguro = HtmlUtils.htmlEscape(nombre);
        return """
                <div style="font-family: Arial, sans-serif; max-width: 520px; margin: auto; color: #1f2933;">
                  <h2 style="color: #0f2a3d;">Recupera tu contraseña</h2>
                  <p>Hola, %s:</p>
                  <p>Recibimos una solicitud para cambiar la contraseña de tu cuenta de UQDelivery.
                     Haz clic en el botón para crear una nueva:</p>
                  <p style="text-align: center; margin: 28px 0;">
                    <a href="%s" style="background: #006a6a; color: #ffffff; padding: 12px 24px;
                       border-radius: 20px; text-decoration: none; font-weight: bold;">Cambiar mi contraseña</a>
                  </p>
                  <p>El enlace vence en <strong>%d minutos</strong> y solo se puede usar una vez.</p>
                  <p style="color: #52606d; font-size: 13px;">Si no fuiste tú, ignora este correo:
                     tu contraseña actual sigue funcionando.</p>
                </div>
                """.formatted(nombreSeguro, enlace, RecuperacionService.MINUTOS_VIGENCIA);
    }
}

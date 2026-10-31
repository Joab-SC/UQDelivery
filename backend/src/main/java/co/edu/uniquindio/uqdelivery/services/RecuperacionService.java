package co.edu.uniquindio.uqdelivery.services;

import co.edu.uniquindio.uqdelivery.config.exception.ReglaNegocioException;
import co.edu.uniquindio.uqdelivery.domain.model.TokenRecuperacion;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.repositories.TokenRecuperacionRepository;
import co.edu.uniquindio.uqdelivery.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Recuperación de contraseña (HU-03).
 *
 * Paso 1 · solicitar(correo): si el correo tiene cuenta, invalida los enlaces anteriores, crea uno nuevo
 *          (UUID, 30 minutos) y lo envía por correo. Si no tiene cuenta, no hace nada. En los dos casos
 *          el controller responde lo mismo, para no revelar qué correos están registrados.
 * Paso 2 · restablecer(token, contraseña, confirmación): si el enlace existe, no se ha usado y no ha
 *          vencido, cambia la contraseña (BCrypt) y marca el enlace como usado.
 */

@Slf4j
@Service
public class RecuperacionService {

    /** Minutos que dura un enlace de recuperación. */
    static final long MINUTOS_VIGENCIA = 30;

    static final String MENSAJE_ENLACE_INVALIDO =
            "El enlace de recuperación no es válido. Solicita uno nuevo";
    static final String MENSAJE_ENLACE_VENCIDO =
            "El enlace de recuperación expiró o ya fue usado. Solicita uno nuevo";

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CorreoService correoService;
    private final String frontendUrl;

    public RecuperacionService(UsuarioRepository usuarioRepository,
                               TokenRecuperacionRepository tokenRepository,
                               PasswordEncoder passwordEncoder,
                               CorreoService correoService,
                               @Value("${app.frontend-url}") String frontendUrl) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.correoService = correoService;
        this.frontendUrl = frontendUrl;
    }

    /** Paso 1. No devuelve nada a propósito: el resultado no debe cambiar la respuesta de la API. */
    @Transactional
    public void solicitar(String correo) {
        String correoNormalizado = correo.trim().toLowerCase(Locale.ROOT);
        Optional<Usuario> encontrado = usuarioRepository.findByCorreo(correoNormalizado);

        if (encontrado.isEmpty()) {
            log.info("Solicitud de recuperación para un correo sin cuenta; no se envía nada");
            return;
        }

        Usuario usuario = encontrado.get();

        // Pedir un enlace nuevo invalida los anteriores que no se usaron
        tokenRepository.invalidarPendientes(usuario);

        TokenRecuperacion token = new TokenRecuperacion(
                usuario,
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusMinutes(MINUTOS_VIGENCIA));
        tokenRepository.save(token);

        String enlace = frontendUrl + "/restablecer?token=" + token.getToken();
        correoService.enviar(usuario.getCorreo(), "Recupera tu contraseña de UQDelivery",
                cuerpoCorreo(usuario.getNombre(), enlace));
    }

    /** Paso 2. Lanza ReglaNegocioException (400) si las contraseñas no coinciden o el enlace no sirve. */
    @Transactional
    public void restablecer(String token, String contrasena, String confirmarContrasena) {
        if (!contrasena.equals(confirmarContrasena)) {
            throw new ReglaNegocioException("Las contraseñas no coinciden");
        }

        TokenRecuperacion tokenRecuperacion = tokenRepository.findByToken(token.trim())
                .orElseThrow(() -> new ReglaNegocioException(MENSAJE_ENLACE_INVALIDO));

        if (!tokenRecuperacion.estaVigente(LocalDateTime.now())) {
            throw new ReglaNegocioException(MENSAJE_ENLACE_VENCIDO);
        }

        Usuario usuario = tokenRecuperacion.getUsuario();
        usuario.cambiarContrasena(passwordEncoder.encode(contrasena));
        tokenRecuperacion.marcarUsado();

        usuarioRepository.save(usuario);
        tokenRepository.save(tokenRecuperacion);
    }

    /** HTML del correo. El nombre se escapa para que no se pueda inyectar HTML en el correo. */
    private String cuerpoCorreo(String nombre, String enlace) {
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
                """.formatted(nombreSeguro, enlace, MINUTOS_VIGENCIA);
    }
}

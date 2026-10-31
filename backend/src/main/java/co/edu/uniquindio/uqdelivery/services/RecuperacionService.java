package co.edu.uniquindio.uqdelivery.services;

import co.edu.uniquindio.uqdelivery.config.exception.ReglaNegocioException;
import co.edu.uniquindio.uqdelivery.domain.datos.EnlaceRecuperacionCreado;
import co.edu.uniquindio.uqdelivery.domain.model.TokenRecuperacion;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.repositories.TokenRecuperacionRepository;
import co.edu.uniquindio.uqdelivery.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Recuperación de contraseña (HU-03).
 *
 * Paso 1 · solicitar(correo): si el correo tiene cuenta (y no pidió otro enlace hace menos de un minuto),
 *          invalida los enlaces anteriores, crea uno nuevo (UUID, 30 minutos) y publica un evento para que
 *          NotificadorRecuperacion envíe el correo en segundo plano. Si no tiene cuenta, no hace nada.
 *          En todos los casos el controller responde lo mismo, para no revelar qué correos están registrados.
 * Paso 2 · restablecer(token, contraseña, confirmación): si el enlace existe, no se ha usado y no ha
 *          vencido, cambia la contraseña (BCrypt) y marca el enlace como usado.
 */
@Slf4j
@Service
public class RecuperacionService {

    /** Minutos que dura un enlace de recuperación. */
    static final long MINUTOS_VIGENCIA = 30;

    /** Tiempo mínimo entre dos enlaces del mismo usuario (evita llenar de correos a alguien). */
    static final long SEGUNDOS_ENTRE_SOLICITUDES = 60;

    /** BCrypt solo tiene en cuenta los primeros 72 bytes; una tilde o una ñ ocupan 2. */
    static final int MAX_BYTES_CONTRASENA = 72;

    static final String MENSAJE_ENLACE_INVALIDO =
            "El enlace de recuperación no es válido. Solicita uno nuevo";
    static final String MENSAJE_ENLACE_VENCIDO =
            "El enlace de recuperación expiró o ya fue usado. Solicita uno nuevo";
    static final String MENSAJE_CONTRASENA_LARGA =
            "La contraseña es demasiado larga. Usa máximo 72 caracteres (las tildes y la ñ cuentan doble)";

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventos;
    private final String frontendUrl;

    public RecuperacionService(UsuarioRepository usuarioRepository,
                               TokenRecuperacionRepository tokenRepository,
                               PasswordEncoder passwordEncoder,
                               ApplicationEventPublisher eventos,
                               @Value("${app.frontend-url}") String frontendUrl) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventos = eventos;
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
        LocalDateTime ahora = LocalDateTime.now();

        // Límite de frecuencia: si ya pidió un enlace hace menos de un minuto, no se crea otro
        if (tokenRepository.existsByUsuarioAndFechaCreacionAfter(usuario, ahora.minusSeconds(SEGUNDOS_ENTRE_SOLICITUDES))) {
            log.info("Solicitud de recuperación repetida en menos de {} s; se ignora", SEGUNDOS_ENTRE_SOLICITUDES);
            return;
        }

        // Pedir un enlace nuevo invalida los anteriores que no se usaron
        tokenRepository.invalidarPendientes(usuario);

        TokenRecuperacion token = new TokenRecuperacion(
                usuario,
                UUID.randomUUID().toString(),
                ahora.plusMinutes(MINUTOS_VIGENCIA));
        tokenRepository.save(token);

        // El correo NO se envía aquí: se publica un evento y NotificadorRecuperacion lo envía en segundo plano
        // cuando la transacción termina. Así la respuesta tarda lo mismo exista o no el correo.
        String enlace = frontendUrl + "/restablecer?token=" + token.getToken();
        eventos.publishEvent(new EnlaceRecuperacionCreado(usuario.getCorreo(), usuario.getNombre(), enlace));
    }

    /** Paso 2. Lanza ReglaNegocioException (400) si las contraseñas no coinciden o el enlace no sirve. */
    @Transactional
    public void restablecer(String token, String contrasena, String confirmarContrasena) {
        if (!contrasena.equals(confirmarContrasena)) {
            throw new ReglaNegocioException("Las contraseñas no coinciden");
        }
        if (contrasena.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_CONTRASENA) {
            throw new ReglaNegocioException(MENSAJE_CONTRASENA_LARGA);
        }

        // findByToken bloquea la fila hasta el final de la transacción (ver TokenRecuperacionRepository)
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
}

package co.edu.uniquindio.uqdelivery.controllers;

import co.edu.uniquindio.uqdelivery.dtos.request.RecuperarRequest;
import co.edu.uniquindio.uqdelivery.dtos.request.RestablecerRequest;
import co.edu.uniquindio.uqdelivery.dtos.response.MensajeResponse;
import co.edu.uniquindio.uqdelivery.services.RecuperacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recuperación de contraseña (HU-03). Comparte /api/auth con HU-01, pero en un controller propio
 */

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Recuperación de contraseña", description = "Enlace por correo para crear una contraseña nueva (HU-03)")
public class RecuperacionController {

    /** Siempre la misma respuesta, exista o no el correo, para no revelar qué correos tienen cuenta. */
    static final String MENSAJE_SOLICITUD =
            "Si el correo está registrado, te enviamos un enlace para restablecer tu contraseña";
    static final String MENSAJE_RESTABLECIDA =
            "Tu contraseña se cambió correctamente. Ya puedes iniciar sesión";

    private final RecuperacionService recuperacionService;

    @PostMapping("/recuperar")
    @Operation(summary = "Pedir un enlace de recuperación (responde igual aunque el correo no exista)")
    public MensajeResponse recuperar(@Valid @RequestBody RecuperarRequest request) {
        recuperacionService.solicitar(request.correo());
        return new MensajeResponse(MENSAJE_SOLICITUD);
    }

    @PostMapping("/restablecer")
    @Operation(summary = "Cambiar la contraseña con el token del enlace (30 minutos, un solo uso)")
    public MensajeResponse restablecer(@Valid @RequestBody RestablecerRequest request) {
        recuperacionService.restablecer(request.token(), request.contrasena(), request.confirmarContrasena());
        return new MensajeResponse(MENSAJE_RESTABLECIDA);
    }
}

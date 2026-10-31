package co.edu.uniquindio.uqdelivery.controllers;

import co.edu.uniquindio.uqdelivery.config.exception.ReglaNegocioException;
import co.edu.uniquindio.uqdelivery.config.security.JwtService;
import co.edu.uniquindio.uqdelivery.config.security.SecurityConfig;
import co.edu.uniquindio.uqdelivery.config.security.UsuarioDetailsService;
import co.edu.uniquindio.uqdelivery.services.RecuperacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba los endpoints de HU-03 con la seguridad real (SecurityConfig) y el service simulado.
 * Comprueba que son públicos (sin token), que validan los campos y que responden con el formato acordado.
 */
@WebMvcTest(RecuperacionController.class)
@Import(SecurityConfig.class)
class RecuperacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecuperacionService recuperacionService;

    // Los necesita el filtro JWT de SecurityConfig; en estas pruebas no se usan.
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void recuperarEsPublicoYSiempreRespondeElMismoMensaje() throws Exception {
        mockMvc.perform(post("/api/auth/recuperar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"correo\": \"cualquiera@correo.co\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(RecuperacionController.MENSAJE_SOLICITUD));

        verify(recuperacionService).solicitar("cualquiera@correo.co");
    }

    @Test
    void recuperarConCorreoMalEscritoResponde400ConElError() throws Exception {
        mockMvc.perform(post("/api/auth/recuperar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"correo\": \"no-es-correo\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Hay campos inválidos"))
                .andExpect(jsonPath("$.errors.correo").value("El formato del correo no es válido"));

        verifyNoInteractions(recuperacionService);
    }

    @Test
    void restablecerConDatosValidosResponde200() throws Exception {
        mockMvc.perform(post("/api/auth/restablecer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "token": "token-123", "contrasena": "NuevaClave2026", "confirmarContrasena": "NuevaClave2026" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(RecuperacionController.MENSAJE_RESTABLECIDA));

        verify(recuperacionService).restablecer("token-123", "NuevaClave2026", "NuevaClave2026");
    }

    @Test
    void restablecerConContrasenaInseguraResponde400() throws Exception {
        mockMvc.perform(post("/api/auth/restablecer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "token": "token-123", "contrasena": "debil", "confirmarContrasena": "debil" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.contrasena").exists());

        verifyNoInteractions(recuperacionService);
    }

    @Test
    void restablecerConEnlaceVencidoResponde400ConElMensajeDelService() throws Exception {
        doThrow(new ReglaNegocioException("El enlace de recuperación expiró o ya fue usado. Solicita uno nuevo"))
                .when(recuperacionService).restablecer("vencido", "NuevaClave2026", "NuevaClave2026");

        mockMvc.perform(post("/api/auth/restablecer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "token": "vencido", "contrasena": "NuevaClave2026", "confirmarContrasena": "NuevaClave2026" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El enlace de recuperación expiró o ya fue usado. Solicita uno nuevo"));
    }
}

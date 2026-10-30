package co.edu.uniquindio.uqdelivery.controllers;

import co.edu.uniquindio.uqdelivery.config.exception.ReglaNegocioException;
import co.edu.uniquindio.uqdelivery.config.mapper.EnvioMapper;
import co.edu.uniquindio.uqdelivery.config.security.JwtService;
import co.edu.uniquindio.uqdelivery.config.security.SecurityConfig;
import co.edu.uniquindio.uqdelivery.config.security.UsuarioDetailsService;
import co.edu.uniquindio.uqdelivery.domain.datos.DatosEnvio;
import co.edu.uniquindio.uqdelivery.domain.model.Ciudad;
import co.edu.uniquindio.uqdelivery.domain.model.Envio;
import co.edu.uniquindio.uqdelivery.domain.model.Rol;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.services.EnvioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba POST /api/envios con la seguridad real (SecurityConfig) y el service simulado.
 * Cubre los dos criterios de HU-04: crear con código de seguimiento y rechazar campos vacíos.
 */
@WebMvcTest(EnvioController.class)
@Import({SecurityConfig.class, EnvioMapper.class})
class EnvioControllerTest {

    private static final String RUTA = "/api/envios";

    private static final String CUERPO_VALIDO = """
            {
              "direccionOrigen": "Carrera 14 # 20-35",
              "ciudadOrigen": "ARMENIA",
              "direccionDestino": "Calle 19 # 8-40",
              "ciudadDestino": "PEREIRA",
              "nombreDestinatario": "Andrés López",
              "telefonoDestinatario": "3151112233",
              "correoDestinatario": "andres.lopez@correo.co",
              "descripcion": "Documentos en sobre de manila",
              "pesoKg": 2.5,
              "largoCm": 35,
              "anchoCm": 25,
              "altoCm": 5
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnvioService envioService;

    // Los necesita el filtro JWT de SecurityConfig; en estas pruebas no se usan.
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    private static Envio envioCreado() {
        Usuario emisor = new Usuario("Emisor", "emisor@uqdelivery.co", "hash", "3000000002", Rol.EMISOR);
        return new Envio("UQD-ABCD2345", emisor,
                "Carrera 14 # 20-35", Ciudad.ARMENIA, "Calle 19 # 8-40", Ciudad.PEREIRA,
                "Andrés López", "3151112233", "andres.lopez@correo.co", "Documentos en sobre de manila",
                new BigDecimal("2.5"), new BigDecimal("35"), new BigDecimal("25"), new BigDecimal("5"),
                new BigDecimal("43.56"), new BigDecimal("16200"));
    }

    @Test
    @WithMockUser(roles = "EMISOR")
    void elEmisorCreaElEnvioYRecibeElCodigoDeSeguimiento() throws Exception {
        when(envioService.crear(any(DatosEnvio.class))).thenReturn(envioCreado());

        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoSeguimiento").value("UQD-ABCD2345"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.costo").value(16200))
                .andExpect(jsonPath("$.ciudadDestino").value("PEREIRA"));
    }

    @Test
    @WithMockUser(roles = "EMISOR")
    void camposObligatoriosVaciosDevuelven400ConElErrorDeCadaCampo() throws Exception {
        String cuerpo = """
                {
                  "direccionOrigen": "",
                  "ciudadDestino": "PEREIRA",
                  "direccionDestino": "Calle 19 # 8-40",
                  "nombreDestinatario": "Andrés López",
                  "telefonoDestinatario": "315-ABC",
                  "correoDestinatario": "no-es-correo",
                  "descripcion": "Documentos",
                  "largoCm": 35,
                  "anchoCm": 25,
                  "altoCm": 5
                }
                """;

        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Hay campos inválidos"))
                .andExpect(jsonPath("$.errors.direccionOrigen").value("La dirección de origen es obligatoria"))
                .andExpect(jsonPath("$.errors.ciudadOrigen").value("La ciudad de origen es obligatoria"))
                .andExpect(jsonPath("$.errors.pesoKg").value("El peso es obligatorio"))
                .andExpect(jsonPath("$.errors.telefonoDestinatario")
                        .value("El teléfono debe tener entre 7 y 15 dígitos"))
                .andExpect(jsonPath("$.errors.correoDestinatario").value("El correo no es válido"));

        verifyNoInteractions(envioService);
    }

    @Test
    @WithMockUser(roles = "EMISOR")
    void elCorreoDelDestinatarioEsOpcional() throws Exception {
        when(envioService.crear(any(DatosEnvio.class))).thenReturn(envioCreado());
        String cuerpo = CUERPO_VALIDO.replace("\"andres.lopez@correo.co\"", "null");

        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "EMISOR")
    void unPaqueteFueraDeLasPoliticasDevuelve400() throws Exception {
        when(envioService.crear(any(DatosEnvio.class)))
                .thenThrow(new ReglaNegocioException("El paquete no cumple con las políticas de tamaño"));

        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El paquete no cumple con las políticas de tamaño"));
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(envioService);
    }

    @Test
    @WithMockUser(roles = "REPARTIDOR")
    void unRolQueNoEsEmisorDevuelve403() throws Exception {
        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isForbidden());

        verifyNoInteractions(envioService);
    }
}
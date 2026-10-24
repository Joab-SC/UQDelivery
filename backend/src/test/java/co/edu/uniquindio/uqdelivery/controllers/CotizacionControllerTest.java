package co.edu.uniquindio.uqdelivery.controllers;

import co.edu.uniquindio.uqdelivery.config.exception.ReglaNegocioException;
import co.edu.uniquindio.uqdelivery.config.mapper.CotizacionMapper;
import co.edu.uniquindio.uqdelivery.config.security.JwtService;
import co.edu.uniquindio.uqdelivery.config.security.SecurityConfig;
import co.edu.uniquindio.uqdelivery.config.security.UsuarioDetailsService;
import co.edu.uniquindio.uqdelivery.domain.datos.Cotizacion;
import co.edu.uniquindio.uqdelivery.domain.model.Ciudad;
import co.edu.uniquindio.uqdelivery.services.CotizacionService;
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
 * Prueba el endpoint con la seguridad real (SecurityConfig) pero sin base de datos:
 * el service y los beans del JWT se reemplazan por mocks.
 */
@WebMvcTest(CotizacionController.class)
@Import({SecurityConfig.class, CotizacionMapper.class})
class CotizacionControllerTest {

    private static final String RUTA = "/api/envios/cotizacion";

    private static final String CUERPO_VALIDO = """
            {
              "ciudadOrigen": "ARMENIA",
              "ciudadDestino": "PEREIRA",
              "pesoKg": 2.5,
              "largoCm": 35,
              "anchoCm": 25,
              "altoCm": 5
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CotizacionService cotizacionService;

    // Los necesita el filtro JWT de SecurityConfig; en estas pruebas no se usan.
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    @WithMockUser(roles = "EMISOR")
    void elEmisorRecibeElCostoYLaDistancia() throws Exception {
        when(cotizacionService.cotizar(Ciudad.ARMENIA, Ciudad.PEREIRA,
                new BigDecimal("2.5"), new BigDecimal("35"), new BigDecimal("25"), new BigDecimal("5")))
                .thenReturn(new Cotizacion(new BigDecimal("16200"), new BigDecimal("43.56")));

        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costo").value(16200))
                .andExpect(jsonPath("$.distanciaKm").value(43.56));
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Debes iniciar sesión para continuar"));

        verifyNoInteractions(cotizacionService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unRolQueNoEsEmisorDevuelve403() throws Exception {
        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("No tienes permiso para realizar esta acción"));

        verifyNoInteractions(cotizacionService);
    }

    @Test
    @WithMockUser(roles = "EMISOR")
    void camposInvalidosDevuelven400ConElErrorDeCadaCampo() throws Exception {
        String cuerpo = """
                {
                  "ciudadDestino": "PEREIRA",
                  "pesoKg": 0,
                  "largoCm": 35,
                  "anchoCm": 25,
                  "altoCm": 5
                }
                """;

        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Hay campos inválidos"))
                .andExpect(jsonPath("$.errors.ciudadOrigen").value("La ciudad de origen es obligatoria"))
                .andExpect(jsonPath("$.errors.pesoKg").value("Debe ser mayor a 0"));

        verifyNoInteractions(cotizacionService);
    }

    @Test
    @WithMockUser(roles = "EMISOR")
    void unaCiudadQueNoExisteDevuelve400() throws Exception {
        String cuerpo = CUERPO_VALIDO.replace("PEREIRA", "BOGOTA");

        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El cuerpo de la petición no es válido"));

        verifyNoInteractions(cotizacionService);
    }

    @Test
    @WithMockUser(roles = "EMISOR")
    void unPaqueteFueraDeLosLimitesDevuelve400() throws Exception {
        when(cotizacionService.cotizar(any(), any(), any(), any(), any(), any()))
                .thenThrow(new ReglaNegocioException("El paquete no cumple con las políticas de tamaño"));

        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El paquete no cumple con las políticas de tamaño"));
    }
}
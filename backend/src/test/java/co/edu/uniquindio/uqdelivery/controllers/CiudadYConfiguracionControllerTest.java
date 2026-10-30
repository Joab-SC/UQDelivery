package co.edu.uniquindio.uqdelivery.controllers;

import co.edu.uniquindio.uqdelivery.config.mapper.EnvioMapper;
import co.edu.uniquindio.uqdelivery.config.security.JwtService;
import co.edu.uniquindio.uqdelivery.config.security.SecurityConfig;
import co.edu.uniquindio.uqdelivery.config.security.UsuarioDetailsService;
import co.edu.uniquindio.uqdelivery.domain.model.ConfiguracionTarifa;
import co.edu.uniquindio.uqdelivery.services.ConfiguracionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** GET /api/ciudades y GET /api/configuracion/politicas: cualquier usuario con sesión (HU-04). */
@WebMvcTest({CiudadController.class, ConfiguracionController.class})
@Import({SecurityConfig.class, EnvioMapper.class})
class CiudadYConfiguracionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConfiguracionService configuracionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    @WithMockUser(roles = "REPARTIDOR")
    void cualquierUsuarioConSesionVeLasCiudades() throws Exception {
        mockMvc.perform(get("/api/ciudades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].codigo").value("ARMENIA"))
                .andExpect(jsonPath("$[3].nombre").value("Calarcá"));
    }

    @Test
    void lasCiudadesPidenSesion() throws Exception {
        mockMvc.perform(get("/api/ciudades")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMISOR")
    void devuelveLosLimitesDelPaquete() throws Exception {
        when(configuracionService.obtener()).thenReturn(new ConfiguracionTarifa(new BigDecimal("5000"),
                new BigDecimal("200"), new BigDecimal("1000"), new BigDecimal("1.4"), new BigDecimal("5"),
                new BigDecimal("50"), new BigDecimal("150")));

        mockMvc.perform(get("/api/configuracion/politicas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pesoMaximoKg").value(50))
                .andExpect(jsonPath("$.medidaMaximaCm").value(150));
    }
}
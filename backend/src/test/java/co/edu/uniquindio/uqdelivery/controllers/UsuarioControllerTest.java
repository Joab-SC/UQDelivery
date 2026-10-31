package co.edu.uniquindio.uqdelivery.controllers;

import co.edu.uniquindio.uqdelivery.config.mapper.UsuarioMapper;
import co.edu.uniquindio.uqdelivery.config.security.JwtService;
import co.edu.uniquindio.uqdelivery.config.security.SecurityConfig;
import co.edu.uniquindio.uqdelivery.config.security.UsuarioDetailsService;
import co.edu.uniquindio.uqdelivery.domain.model.Rol;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.services.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** GET /api/usuarios y PATCH /api/usuarios/{id}/rol: solo ADMIN (HU-02). */
@WebMvcTest(UsuarioController.class)
@Import({SecurityConfig.class, UsuarioMapper.class})
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void elAdminListaLosUsuarios() throws Exception {
        when(usuarioService.listar()).thenReturn(List.of(
                new Usuario("Ana", "ana@uqdelivery.co", "hash", "3001112233", Rol.EMISOR)));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].correo").value("ana@uqdelivery.co"))
                .andExpect(jsonPath("$[0].rol").value("EMISOR"));
    }

    @Test
    @WithMockUser(roles = "EMISOR")
    void unEmisorNoPuedeListarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios")).andExpect(status().isForbidden());
    }

    @Test
    void listarPideSesion() throws Exception {
        mockMvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void elAdminCambiaElRol() throws Exception {
        when(usuarioService.cambiarRol(2L, Rol.REPARTIDOR)).thenReturn(
                new Usuario("Ana", "ana@uqdelivery.co", "hash", "3001112233", Rol.REPARTIDOR));

        mockMvc.perform(patch("/api/usuarios/2/rol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rol\":\"REPARTIDOR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("REPARTIDOR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cambiarRolSinRolEsInvalido() throws Exception {
        mockMvc.perform(patch("/api/usuarios/2/rol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.rol").value("El rol es obligatorio"));
    }

    @Test
    @WithMockUser(roles = "REPARTIDOR")
    void unNoAdminNoPuedeCambiarRoles() throws Exception {
        mockMvc.perform(patch("/api/usuarios/2/rol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rol\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
    }
}

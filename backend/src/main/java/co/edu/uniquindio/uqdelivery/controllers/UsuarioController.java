package co.edu.uniquindio.uqdelivery.controllers;

import co.edu.uniquindio.uqdelivery.config.mapper.UsuarioMapper;
import co.edu.uniquindio.uqdelivery.dtos.request.CambiarRolRequest;
import co.edu.uniquindio.uqdelivery.dtos.response.UsuarioResponse;
import co.edu.uniquindio.uqdelivery.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Gestión de usuarios y roles. Solo ADMIN. */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Consulta y cambio de roles de los usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar().stream().map(usuarioMapper::toResponse).toList();
    }

    @PatchMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar el rol de un usuario")
    public UsuarioResponse cambiarRol(@PathVariable Long id, @Valid @RequestBody CambiarRolRequest request) {
        return usuarioMapper.toResponse(usuarioService.cambiarRol(id, request.rol()));
    }
}

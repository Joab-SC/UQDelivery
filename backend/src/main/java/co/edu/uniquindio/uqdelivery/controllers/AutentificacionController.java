package co.edu.uniquindio.uqdelivery.controllers;

import co.edu.uniquindio.uqdelivery.config.mapper.UsuarioMapper;
import co.edu.uniquindio.uqdelivery.domain.datos.ResultadoAutenticacion;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.dtos.request.LoginRequest;
import co.edu.uniquindio.uqdelivery.dtos.request.RegistroRequest;
import co.edu.uniquindio.uqdelivery.dtos.response.LoginResponse;
import co.edu.uniquindio.uqdelivery.dtos.response.UsuarioResponse;
import co.edu.uniquindio.uqdelivery.services.AutenticacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AutentificacionController {

    private final AutenticacionService authService;
    private final UsuarioMapper usuarioMapper;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        Usuario usuario = authService.registrar(
                request.nombre(),
                request.correo(),
                request.telefono(),
                request.contrasena(),
                request.confirmarContrasena()
        );
        UsuarioResponse response = usuarioMapper.toResponse(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        ResultadoAutenticacion resultado = authService.login(
                request.correo(),
                request.contrasena()
        );

        UsuarioResponse usuarioResponse = usuarioMapper.toResponse(resultado.usuario());
        LoginResponse response = new LoginResponse(resultado.token(), usuarioResponse);

        return ResponseEntity.ok(response);
    }
}
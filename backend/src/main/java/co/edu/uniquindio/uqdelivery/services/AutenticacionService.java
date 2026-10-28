package co.edu.uniquindio.uqdelivery.services;

import co.edu.uniquindio.uqdelivery.config.exception.ConflictoException;
import co.edu.uniquindio.uqdelivery.config.exception.ReglaNegocioException;
import co.edu.uniquindio.uqdelivery.config.security.JwtService;
import co.edu.uniquindio.uqdelivery.domain.datos.ResultadoAutenticacion;
import co.edu.uniquindio.uqdelivery.domain.model.Rol;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutenticacionService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public Usuario registrar(String nombre, String correo, String telefono, String contrasena, String confirmarContrasena) {
        // Validar que las contraseñas coincidan
        if (!contrasena.equals(confirmarContrasena)) {
            throw new ReglaNegocioException("Las contraseñas no coinciden");
        }

        // Correo en minúsculas y validación de unicidad
        String correoNormalizado = correo.toLowerCase().trim();
        if (usuarioRepository.existsByCorreo(correoNormalizado)) {
            throw new ConflictoException("El correo ya se encuentra registrado");
        }

        // Encriptar contraseña con BCrypt
        String contrasenaHash = passwordEncoder.encode(contrasena);

        // Crear la entidad (por defecto siempre crea rol EMISOR en HU-01)
        Usuario nuevoUsuario = new Usuario(
                nombre,
                correoNormalizado,
                contrasenaHash,
                telefono,
                Rol.EMISOR
        );

        return usuarioRepository.save(nuevoUsuario);
    }

    public ResultadoAutenticacion login(String correo, String contrasena) {
        String correoNormalizado = correo.toLowerCase().trim();

        // Autenticar credenciales mediante Spring Security (si falla, lanza BadCredentialsException -> 401)[cite: 1]
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(correoNormalizado, contrasena)
        );

        // Buscar el usuario en la base de datos
        Usuario usuario = usuarioRepository.findByCorreo(correoNormalizado)
                .orElseThrow(() -> new ReglaNegocioException("Usuario no encontrado"));

        // Generar el token JWT (vigencia de 2 horas configurada en el sistema)[cite: 1]
        String token = jwtService.generarToken(usuario);

        return new ResultadoAutenticacion(token, usuario);
    }
}
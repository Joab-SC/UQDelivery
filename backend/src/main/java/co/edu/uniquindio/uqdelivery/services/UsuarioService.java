package co.edu.uniquindio.uqdelivery.services;

import co.edu.uniquindio.uqdelivery.config.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.config.exception.ReglaNegocioException;
import co.edu.uniquindio.uqdelivery.config.security.UsuarioAutenticado;
import co.edu.uniquindio.uqdelivery.domain.model.Rol;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticado usuarioAutenticado;

    /** Todos los usuarios, ordenados por nombre */
    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAll(Sort.by("nombre"));
    }

    /**
     * Cambia el rol de un usuario. El administrador no puede cambiar su propio rol,
     * para no quedarse sin administradores.
     */
    @Transactional
    public Usuario cambiarRol(Long id, Rol nuevoRol) {
        Usuario administrador = usuarioAutenticado.obtener();
        if (administrador.getId().equals(id)) {
            throw new ReglaNegocioException("No puedes cambiar tu propio rol");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        usuario.cambiarRol(nuevoRol);
        return usuarioRepository.save(usuario);
    }
}

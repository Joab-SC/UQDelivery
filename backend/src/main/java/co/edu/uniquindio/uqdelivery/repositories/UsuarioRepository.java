package co.edu.uniquindio.uqdelivery.repositories;

import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Lo usa la seguridad para cargar al usuario del token. El correo se guarda en minúsculas. */
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
}

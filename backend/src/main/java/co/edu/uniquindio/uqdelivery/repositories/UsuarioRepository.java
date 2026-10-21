package co.edu.uniquindio.uqdelivery.repositories;

import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}

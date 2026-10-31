package co.edu.uniquindio.uqdelivery.repositories;

import co.edu.uniquindio.uqdelivery.domain.model.TokenRecuperacion;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {

    /**
     * HU-03: busca el enlace que llega desde el correo (?token=...) y BLOQUEA la fila hasta que termine la
     * transacción (SELECT ... FOR UPDATE). Si llegan dos peticiones a la vez con el mismo token, la segunda
     * espera a la primera y luego ve el token ya usado: así la contraseña no se puede cambiar dos veces.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TokenRecuperacion> findByToken(String token);

    /** HU-03: ¿el usuario pidió un enlace hace poco? Sirve para limitar la frecuencia de solicitudes. */
    boolean existsByUsuarioAndFechaCreacionAfter(Usuario usuario, LocalDateTime desde);

    /**
     * HU-03: al pedir un enlace nuevo, los anteriores del usuario que no se hayan usado quedan inservibles.
     * Consulta con parámetros (nunca texto concatenado), así no hay inyección SQL.
     */
    @Modifying
    @Query("UPDATE TokenRecuperacion t SET t.usado = true WHERE t.usuario = :usuario AND t.usado = false")
    int invalidarPendientes(@Param("usuario") Usuario usuario);
}

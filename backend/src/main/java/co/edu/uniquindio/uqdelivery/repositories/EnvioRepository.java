package co.edu.uniquindio.uqdelivery.repositories;

import co.edu.uniquindio.uqdelivery.domain.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvioRepository extends JpaRepository<Envio, Long> {

    /** Lo usa HU-04 para no repetir un código de seguimiento al crear un envío. */
    boolean existsByCodigoSeguimiento(String codigoSeguimiento);
}
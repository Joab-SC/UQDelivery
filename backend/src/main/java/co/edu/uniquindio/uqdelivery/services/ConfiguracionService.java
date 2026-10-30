package co.edu.uniquindio.uqdelivery.services;

import co.edu.uniquindio.uqdelivery.config.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.domain.model.ConfiguracionTarifa;
import co.edu.uniquindio.uqdelivery.repositories.ConfiguracionTarifaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lectura de la configuración de tarifas y límites (una sola fila, id = 1).
 * HU-04 la usa para mostrar los límites del paquete en el formulario. HU-16 podrá agregar aquí la edición.
 */
@Service
@RequiredArgsConstructor
public class ConfiguracionService {

    private final ConfiguracionTarifaRepository configuracionTarifaRepository;

    @Transactional(readOnly = true)
    public ConfiguracionTarifa obtener() {
        return configuracionTarifaRepository.findById(ConfiguracionTarifa.ID_UNICO)
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay configuración de tarifas"));
    }
}

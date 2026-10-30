package co.edu.uniquindio.uqdelivery.services;

import co.edu.uniquindio.uqdelivery.config.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.domain.model.ConfiguracionTarifa;
import co.edu.uniquindio.uqdelivery.repositories.ConfiguracionTarifaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfiguracionServiceTest {

    @Mock
    private ConfiguracionTarifaRepository configuracionTarifaRepository;

    @InjectMocks
    private ConfiguracionService configuracionService;

    @Test
    void devuelveLaFilaUnicaDeConfiguracion() {
        ConfiguracionTarifa configuracion = new ConfiguracionTarifa(new BigDecimal("5000"), new BigDecimal("200"),
                new BigDecimal("1000"), new BigDecimal("1.4"), new BigDecimal("5"), new BigDecimal("50"),
                new BigDecimal("150"));
        when(configuracionTarifaRepository.findById(ConfiguracionTarifa.ID_UNICO))
                .thenReturn(Optional.of(configuracion));

        assertThat(configuracionService.obtener()).isSameAs(configuracion);
    }

    @Test
    void siNoHayConfiguracionLanza404() {
        when(configuracionTarifaRepository.findById(ConfiguracionTarifa.ID_UNICO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configuracionService.obtener())
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No hay configuración de tarifas");
    }
}

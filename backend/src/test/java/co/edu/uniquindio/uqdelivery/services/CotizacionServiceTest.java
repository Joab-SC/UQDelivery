package co.edu.uniquindio.uqdelivery.services;

import co.edu.uniquindio.uqdelivery.config.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.config.exception.ReglaNegocioException;
import co.edu.uniquindio.uqdelivery.domain.datos.Cotizacion;
import co.edu.uniquindio.uqdelivery.domain.model.Ciudad;
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
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CotizacionServiceTest {

    @Mock
    private ConfiguracionTarifaRepository configuracionTarifaRepository;

    @InjectMocks
    private CotizacionService cotizacionService;

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    /** Las mismas tarifas de data.sql. */
    private void conTarifasPorDefecto() {
        when(configuracionTarifaRepository.findById(ConfiguracionTarifa.ID_UNICO)).thenReturn(Optional.of(
                new ConfiguracionTarifa(bd("5000"), bd("200"), bd("1000"), bd("1.4"),
                        bd("5"), bd("50"), bd("150"))));
    }

    /** Solo cobra la tarifa base, para probar el redondeo con valores exactos. */
    private void conSoloTarifaBase(String tarifaBase) {
        when(configuracionTarifaRepository.findById(ConfiguracionTarifa.ID_UNICO)).thenReturn(Optional.of(
                new ConfiguracionTarifa(bd(tarifaBase), bd("0"), bd("0"), bd("1.4"),
                        bd("5"), bd("50"), bd("150"))));
    }

    @Test
    void entreCiudadesDistintasAplicaHaversinePorElFactorDeRuta() {
        conTarifasPorDefecto();

        Cotizacion cotizacion = cotizacionService.cotizar(Ciudad.ARMENIA, Ciudad.PEREIRA,
                bd("2.5"), bd("35"), bd("25"), bd("5"));

        assertThat(cotizacion.distanciaKm()).isCloseTo(bd("43.56"), within(bd("0.01")));
        assertThat(cotizacion.costo()).isEqualByComparingTo("16200");
    }

    @Test
    void enLaMismaCiudadCobraLaDistanciaMinima() {
        conTarifasPorDefecto();

        Cotizacion cotizacion = cotizacionService.cotizar(Ciudad.ARMENIA, Ciudad.ARMENIA,
                bd("1"), bd("20"), bd("15"), bd("10"));

        assertThat(cotizacion.distanciaKm()).isEqualByComparingTo("5.00");
        assertThat(cotizacion.costo()).isEqualByComparingTo("7000");
    }

    @Test
    void redondeaALaCentenaHaciaArribaEnLaMitad() {
        conSoloTarifaBase("12350");

        Cotizacion cotizacion = cotizacionService.cotizar(Ciudad.ARMENIA, Ciudad.ARMENIA,
                bd("1"), bd("10"), bd("10"), bd("10"));

        assertThat(cotizacion.costo()).isEqualByComparingTo("12400");
    }

    @Test
    void redondeaALaCentenaHaciaAbajoPorDebajoDeLaMitad() {
        conSoloTarifaBase("12349");

        Cotizacion cotizacion = cotizacionService.cotizar(Ciudad.ARMENIA, Ciudad.ARMENIA,
                bd("1"), bd("10"), bd("10"), bd("10"));

        assertThat(cotizacion.costo()).isEqualByComparingTo("12300");
    }

    @Test
    void aceptaUnPaqueteExactamenteEnLosLimites() {
        conTarifasPorDefecto();

        Cotizacion cotizacion = cotizacionService.cotizar(Ciudad.ARMENIA, Ciudad.PEREIRA,
                bd("50"), bd("150"), bd("150"), bd("150"));

        assertThat(cotizacion.costo()).isPositive();
    }

    @Test
    void rechazaUnPesoMayorAlMaximo() {
        conTarifasPorDefecto();

        assertThatThrownBy(() -> cotizacionService.cotizar(Ciudad.ARMENIA, Ciudad.PEREIRA,
                bd("50.01"), bd("10"), bd("10"), bd("10")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El paquete no cumple con las políticas de tamaño");
    }

    @Test
    void rechazaUnaMedidaMayorAlMaximo() {
        conTarifasPorDefecto();

        assertThatThrownBy(() -> cotizacionService.cotizar(Ciudad.ARMENIA, Ciudad.PEREIRA,
                bd("1"), bd("10"), bd("150.01"), bd("10")))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void fallaSiNoExisteLaConfiguracionDeTarifas() {
        when(configuracionTarifaRepository.findById(ConfiguracionTarifa.ID_UNICO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cotizacionService.cotizar(Ciudad.ARMENIA, Ciudad.PEREIRA,
                bd("1"), bd("10"), bd("10"), bd("10")))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
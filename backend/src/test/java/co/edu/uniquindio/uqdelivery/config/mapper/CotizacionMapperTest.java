package co.edu.uniquindio.uqdelivery.config.mapper;

import co.edu.uniquindio.uqdelivery.domain.datos.Cotizacion;
import co.edu.uniquindio.uqdelivery.dtos.response.CotizacionResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CotizacionMapperTest {

    @Test
    void convierteLaCotizacionEnSuRespuesta() {
        Cotizacion cotizacion = new Cotizacion(new BigDecimal("16200"), new BigDecimal("43.56"));

        CotizacionResponse respuesta = new CotizacionMapper().toResponse(cotizacion);

        assertThat(respuesta).isEqualTo(new CotizacionResponse(new BigDecimal("16200"), new BigDecimal("43.56")));
    }
}
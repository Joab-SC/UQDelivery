package co.edu.uniquindio.uqdelivery.config.mapper;

import co.edu.uniquindio.uqdelivery.domain.datos.Cotizacion;
import co.edu.uniquindio.uqdelivery.dtos.response.CotizacionResponse;
import org.springframework.stereotype.Component;

@Component
public class CotizacionMapper {

    public CotizacionResponse toResponse(Cotizacion cotizacion) {
        return new CotizacionResponse(cotizacion.costo(), cotizacion.distanciaKm());
    }
}
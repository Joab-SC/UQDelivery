package co.edu.uniquindio.uqdelivery.config.mapper;

import co.edu.uniquindio.uqdelivery.domain.datos.DatosEnvio;
import co.edu.uniquindio.uqdelivery.domain.model.Ciudad;
import co.edu.uniquindio.uqdelivery.domain.model.ConfiguracionTarifa;
import co.edu.uniquindio.uqdelivery.domain.model.Envio;
import co.edu.uniquindio.uqdelivery.dtos.request.EnvioRequest;
import co.edu.uniquindio.uqdelivery.dtos.response.CiudadResponse;
import co.edu.uniquindio.uqdelivery.dtos.response.EnvioResponse;
import co.edu.uniquindio.uqdelivery.dtos.response.PoliticasResponse;
import org.springframework.stereotype.Component;

/** Conversiones del dominio de envíos (HU-04). */
@Component
public class EnvioMapper {

    public DatosEnvio toDatos(EnvioRequest request) {
        return new DatosEnvio(
                request.direccionOrigen(), request.ciudadOrigen(),
                request.direccionDestino(), request.ciudadDestino(),
                request.nombreDestinatario(), request.telefonoDestinatario(), request.correoDestinatario(),
                request.descripcion(),
                request.pesoKg(), request.largoCm(), request.anchoCm(), request.altoCm());
    }

    public EnvioResponse toResponse(Envio envio) {
        return new EnvioResponse(
                envio.getId(),
                envio.getCodigoSeguimiento(),
                envio.getDireccionOrigen(), envio.getCiudadOrigen(),
                envio.getDireccionDestino(), envio.getCiudadDestino(),
                envio.getNombreDestinatario(), envio.getTelefonoDestinatario(), envio.getCorreoDestinatario(),
                envio.getDescripcion(),
                envio.getPesoKg(), envio.getLargoCm(), envio.getAnchoCm(), envio.getAltoCm(),
                envio.getDistanciaKm(), envio.getCosto(),
                envio.getEstado(),
                envio.getFechaCreacion());
    }

    public CiudadResponse toResponse(Ciudad ciudad) {
        return new CiudadResponse(ciudad.name(), ciudad.getNombre());
    }

    public PoliticasResponse toPoliticasResponse(ConfiguracionTarifa configuracion) {
        return new PoliticasResponse(configuracion.getPesoMaximoKg(), configuracion.getMedidaMaximaCm());
    }
}

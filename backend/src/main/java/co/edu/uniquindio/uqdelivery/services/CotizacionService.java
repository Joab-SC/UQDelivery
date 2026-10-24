package co.edu.uniquindio.uqdelivery.services;

import co.edu.uniquindio.uqdelivery.config.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.config.exception.ReglaNegocioException;
import co.edu.uniquindio.uqdelivery.domain.datos.Cotizacion;
import co.edu.uniquindio.uqdelivery.domain.model.Ciudad;
import co.edu.uniquindio.uqdelivery.domain.model.ConfiguracionTarifa;
import co.edu.uniquindio.uqdelivery.repositories.ConfiguracionTarifaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calcula la distancia y el costo de un envío (HU-05). HU-04 lo usa para guardar el costo del envío.
 * distancia = max(Haversine entre ciudades × factorRuta, distanciaMinimaKm)
 * costo = tarifaBase + km × precioPorKm + kg × precioPorKg, redondeado a la centena (HALF_UP).
 */
@Service
@RequiredArgsConstructor
public class CotizacionService {

    private static final double RADIO_TIERRA_KM = 6371.0;
    private static final BigDecimal CENTENA = BigDecimal.valueOf(100);

    private final ConfiguracionTarifaRepository configuracionTarifaRepository;

    public Cotizacion cotizar(Ciudad origen, Ciudad destino, BigDecimal pesoKg,
                              BigDecimal largoCm, BigDecimal anchoCm, BigDecimal altoCm) {
        ConfiguracionTarifa config = configuracionTarifaRepository.findById(ConfiguracionTarifa.ID_UNICO)
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay configuración de tarifas"));

        if (!config.admitePaquete(pesoKg, largoCm, anchoCm, altoCm)) {
            throw new ReglaNegocioException("El paquete no cumple con las políticas de tamaño");
        }

        BigDecimal distanciaKm = BigDecimal.valueOf(haversineKm(origen, destino))
                .multiply(config.getFactorRuta())
                .max(config.getDistanciaMinimaKm())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal costo = config.getTarifaBase()
                .add(distanciaKm.multiply(config.getPrecioPorKm()))
                .add(pesoKg.multiply(config.getPrecioPorKg()))
                .divide(CENTENA, 0, RoundingMode.HALF_UP)
                .multiply(CENTENA);

        return new Cotizacion(costo, distanciaKm);
    }

    /** Distancia en línea recta entre los centros de dos ciudades. */
    private double haversineKm(Ciudad a, Ciudad b) {
        double dLat = Math.toRadians(b.getLatitud() - a.getLatitud());
        double dLon = Math.toRadians(b.getLongitud() - a.getLongitud());
        double h = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(a.getLatitud())) * Math.cos(Math.toRadians(b.getLatitud()))
                * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * RADIO_TIERRA_KM * Math.asin(Math.sqrt(h));
    }
}
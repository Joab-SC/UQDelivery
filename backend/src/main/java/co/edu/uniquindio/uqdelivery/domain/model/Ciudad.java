package co.edu.uniquindio.uqdelivery.domain.model;

import lombok.Getter;

/**
 * Ciudades donde opera UQDelivery, con las coordenadas de su centro (grados decimales).
 * Las coordenadas sirven para estimar la distancia entre ciudades en la cotización (HU-05).
 */
@Getter
public enum Ciudad {
    ARMENIA("Armenia", 4.5339, -75.6811),
    PEREIRA("Pereira", 4.8133, -75.6961),
    MANIZALES("Manizales", 5.0689, -75.5174),
    CALARCA("Calarcá", 4.5296, -75.6436),
    MONTENEGRO("Montenegro", 4.5662, -75.7505);

    private final String nombre;
    private final double latitud;
    private final double longitud;

    Ciudad(String nombre, double latitud, double longitud) {
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
    }
}

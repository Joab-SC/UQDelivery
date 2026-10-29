package co.edu.uniquindio.uqdelivery.services;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Genera códigos de seguimiento con el formato UQD-XXXXXXXX (HU-04).
 * Usa solo mayúsculas y números sin caracteres que se confunden al leerlos (sin O, 0, I ni 1).
 * No revisa si el código ya existe: eso lo hace EnvioService con el repository.
 */
@Component
public class GeneradorCodigoSeguimiento {

    static final String PREFIJO = "UQD-";
    static final String CARACTERES = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    static final int LONGITUD = 8;

    private final SecureRandom aleatorio = new SecureRandom();

    public String generar() {
        StringBuilder codigo = new StringBuilder(PREFIJO);
        for (int i = 0; i < LONGITUD; i++) {
            codigo.append(CARACTERES.charAt(aleatorio.nextInt(CARACTERES.length())));
        }
        return codigo.toString();
    }
}
package co.edu.uniquindio.uqdelivery.services;

import co.edu.uniquindio.uqdelivery.config.security.UsuarioAutenticado;
import co.edu.uniquindio.uqdelivery.domain.datos.Cotizacion;
import co.edu.uniquindio.uqdelivery.domain.datos.DatosEnvio;
import co.edu.uniquindio.uqdelivery.domain.model.Envio;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.repositories.EnvioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Creación de solicitudes de envío (HU-04).
 * - El emisor sale del token (UsuarioAutenticado), nunca del body.
 * - El costo y la distancia los calcula siempre CotizacionService (HU-05); también valida los límites del paquete.
 * - El envío nace en PENDIENTE con un código de seguimiento único.
 */
@Service
@RequiredArgsConstructor
public class EnvioService {

    /** Con 32^8 combinaciones, repetir 10 veces seguidas es prácticamente imposible. */
    static final int MAX_INTENTOS_CODIGO = 10;

    private final EnvioRepository envioRepository;
    private final CotizacionService cotizacionService;
    private final UsuarioAutenticado usuarioAutenticado;
    private final GeneradorCodigoSeguimiento generadorCodigo;

    @Transactional
    public Envio crear(DatosEnvio datos) {
        Usuario emisor = usuarioAutenticado.obtener();

        Cotizacion cotizacion = cotizacionService.cotizar(
                datos.ciudadOrigen(), datos.ciudadDestino(),
                datos.pesoKg(), datos.largoCm(), datos.anchoCm(), datos.altoCm());

        Envio envio = new Envio(
                generarCodigoUnico(), emisor,
                limpiar(datos.direccionOrigen()), datos.ciudadOrigen(),
                limpiar(datos.direccionDestino()), datos.ciudadDestino(),
                limpiar(datos.nombreDestinatario()), limpiar(datos.telefonoDestinatario()),
                normalizarCorreo(datos.correoDestinatario()),
                limpiar(datos.descripcion()),
                datos.pesoKg(), datos.largoCm(), datos.anchoCm(), datos.altoCm(),
                cotizacion.distanciaKm(), cotizacion.costo());

        return envioRepository.save(envio);
    }

    private String generarCodigoUnico() {
        for (int intento = 0; intento < MAX_INTENTOS_CODIGO; intento++) {
            String codigo = generadorCodigo.generar();
            if (!envioRepository.existsByCodigoSeguimiento(codigo)) {
                return codigo;
            }
        }
        throw new IllegalStateException("No se pudo generar un código de seguimiento único");
    }

    private static String limpiar(String texto) {
        return texto == null ? null : texto.trim();
    }

    /** El correo es opcional: si llega vacío se guarda null; si llega, en minúsculas. */
    private static String normalizarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return null;
        }
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}

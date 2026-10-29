package co.edu.uniquindio.uqdelivery.services;

import co.edu.uniquindio.uqdelivery.config.exception.ReglaNegocioException;
import co.edu.uniquindio.uqdelivery.config.security.UsuarioAutenticado;
import co.edu.uniquindio.uqdelivery.domain.datos.Cotizacion;
import co.edu.uniquindio.uqdelivery.domain.datos.DatosEnvio;
import co.edu.uniquindio.uqdelivery.domain.model.Ciudad;
import co.edu.uniquindio.uqdelivery.domain.model.Envio;
import co.edu.uniquindio.uqdelivery.domain.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.domain.model.Rol;
import co.edu.uniquindio.uqdelivery.domain.model.Usuario;
import co.edu.uniquindio.uqdelivery.repositories.EnvioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private CotizacionService cotizacionService;

    @Mock
    private UsuarioAutenticado usuarioAutenticado;

    @Mock
    private GeneradorCodigoSeguimiento generadorCodigo;

    @InjectMocks
    private EnvioService envioService;

    private Usuario emisor;

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    private static DatosEnvio datos(String correoDestinatario) {
        return new DatosEnvio(
                "  Carrera 14 # 20-35  ", Ciudad.ARMENIA,
                "Calle 19 # 8-40", Ciudad.PEREIRA,
                "Andrés López", "3151112233", correoDestinatario,
                "Documentos en sobre de manila",
                bd("2.5"), bd("35"), bd("25"), bd("5"));
    }

    @BeforeEach
    void preparar() {
        emisor = new Usuario("Emisor de prueba", "emisor@uqdelivery.co", "hash", "3000000002", Rol.EMISOR);
    }

    /** Lo común a los casos que sí crean el envío. */
    private void conEmisorYCotizacion() {
        when(usuarioAutenticado.obtener()).thenReturn(emisor);
        when(cotizacionService.cotizar(Ciudad.ARMENIA, Ciudad.PEREIRA, bd("2.5"), bd("35"), bd("25"), bd("5")))
                .thenReturn(new Cotizacion(bd("16200"), bd("43.56")));
        when(envioRepository.save(any(Envio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void creaElEnvioPendienteConElEmisorDelTokenYElCostoDeLaCotizacion() {
        conEmisorYCotizacion();
        when(generadorCodigo.generar()).thenReturn("UQD-ABCD2345");
        when(envioRepository.existsByCodigoSeguimiento("UQD-ABCD2345")).thenReturn(false);

        Envio envio = envioService.crear(datos("Andres.Lopez@Correo.co"));

        assertThat(envio.getCodigoSeguimiento()).isEqualTo("UQD-ABCD2345");
        assertThat(envio.getEmisor()).isSameAs(emisor);
        assertThat(envio.getEstado()).isEqualTo(EstadoEnvio.PENDIENTE);
        assertThat(envio.getCosto()).isEqualByComparingTo("16200");
        assertThat(envio.getDistanciaKm()).isEqualByComparingTo("43.56");
        assertThat(envio.getRepartidor()).isNull();
        verify(envioRepository).save(envio);
    }

    @Test
    void limpiaLosTextosYGuardaElCorreoEnMinusculas() {
        conEmisorYCotizacion();
        when(generadorCodigo.generar()).thenReturn("UQD-ABCD2345");

        Envio envio = envioService.crear(datos("  Andres.Lopez@Correo.co "));

        assertThat(envio.getDireccionOrigen()).isEqualTo("Carrera 14 # 20-35");
        assertThat(envio.getCorreoDestinatario()).isEqualTo("andres.lopez@correo.co");
    }

    @Test
    void siElCorreoLlegaVacioSeGuardaNulo() {
        conEmisorYCotizacion();
        when(generadorCodigo.generar()).thenReturn("UQD-ABCD2345");

        Envio envio = envioService.crear(datos("   "));

        assertThat(envio.getCorreoDestinatario()).isNull();
    }

    @Test
    void siElCodigoYaExisteGeneraOtro() {
        conEmisorYCotizacion();
        when(generadorCodigo.generar()).thenReturn("UQD-REPETIDO", "UQD-NUEV2345");
        when(envioRepository.existsByCodigoSeguimiento("UQD-REPETIDO")).thenReturn(true);
        when(envioRepository.existsByCodigoSeguimiento("UQD-NUEV2345")).thenReturn(false);

        Envio envio = envioService.crear(datos(null));

        assertThat(envio.getCodigoSeguimiento()).isEqualTo("UQD-NUEV2345");
        verify(generadorCodigo, times(2)).generar();
    }

    @Test
    void siNoLograUnCodigoUnicoNoGuarda() {
        when(usuarioAutenticado.obtener()).thenReturn(emisor);
        when(cotizacionService.cotizar(any(), any(), any(), any(), any(), any()))
                .thenReturn(new Cotizacion(bd("16200"), bd("43.56")));
        when(generadorCodigo.generar()).thenReturn("UQD-REPETIDO");
        when(envioRepository.existsByCodigoSeguimiento(anyString())).thenReturn(true);

        assertThatThrownBy(() -> envioService.crear(datos(null)))
                .isInstanceOf(IllegalStateException.class);

        verify(generadorCodigo, times(EnvioService.MAX_INTENTOS_CODIGO)).generar();
        verify(envioRepository, never()).save(any());
    }

    @Test
    void siElPaqueteNoCumpleLasPoliticasNoSeCreaElEnvio() {
        when(usuarioAutenticado.obtener()).thenReturn(emisor);
        when(cotizacionService.cotizar(any(), any(), any(), any(), any(), any()))
                .thenThrow(new ReglaNegocioException("El paquete no cumple con las políticas de tamaño"));

        assertThatThrownBy(() -> envioService.crear(datos(null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El paquete no cumple con las políticas de tamaño");

        verify(generadorCodigo, never()).generar();
        verify(envioRepository, never()).save(any());
    }
}

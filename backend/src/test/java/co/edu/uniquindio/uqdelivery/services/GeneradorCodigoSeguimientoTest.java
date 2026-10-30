package co.edu.uniquindio.uqdelivery.services;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GeneradorCodigoSeguimientoTest {

    private final GeneradorCodigoSeguimiento generador = new GeneradorCodigoSeguimiento();

    @Test
    void generaElFormatoUqdSinCaracteresConfusos() {
        for (int i = 0; i < 1000; i++) {
            String codigo = generador.generar();
            assertThat(codigo).hasSize(12).matches("^UQD-[A-HJ-NP-Z2-9]{8}$");
        }
    }

    @Test
    void generaCodigosDistintos() {
        Set<String> codigos = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            codigos.add(generador.generar());
        }
        assertThat(codigos).hasSize(1000);
    }
}
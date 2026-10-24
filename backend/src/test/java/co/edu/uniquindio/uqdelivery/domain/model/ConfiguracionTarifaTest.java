package co.edu.uniquindio.uqdelivery.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguracionTarifaTest {

    private final ConfiguracionTarifa config = new ConfiguracionTarifa(
            new BigDecimal("5000"), new BigDecimal("200"), new BigDecimal("1000"),
            new BigDecimal("1.4"), new BigDecimal("5"), new BigDecimal("50"), new BigDecimal("150"));

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    @Test
    void admiteUnPaqueteDentroDeLosLimites() {
        assertThat(config.admitePaquete(bd("2.5"), bd("35"), bd("25"), bd("5"))).isTrue();
    }

    @Test
    void admiteUnPaqueteExactamenteEnElLimite() {
        assertThat(config.admitePaquete(bd("50"), bd("150"), bd("150"), bd("150"))).isTrue();
    }

    @Test
    void rechazaUnPesoMayorAlMaximo() {
        assertThat(config.admitePaquete(bd("50.01"), bd("10"), bd("10"), bd("10"))).isFalse();
    }

    @Test
    void rechazaCualquierMedidaMayorAlMaximo() {
        assertThat(config.admitePaquete(bd("1"), bd("150.01"), bd("10"), bd("10"))).isFalse();
        assertThat(config.admitePaquete(bd("1"), bd("10"), bd("150.01"), bd("10"))).isFalse();
        assertThat(config.admitePaquete(bd("1"), bd("10"), bd("10"), bd("150.01"))).isFalse();
    }
}
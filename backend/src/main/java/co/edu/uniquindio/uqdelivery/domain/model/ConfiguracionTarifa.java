package co.edu.uniquindio.uqdelivery.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tarifas y límites del paquete que usa la cotización (HU-05).
 * La tabla tiene una sola fila, siempre con id = 1. El admin la gestionará en HU-16.
 */
@Entity
@Table(name = "configuracion_tarifa")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConfiguracionTarifa {

    /** Id de la única fila de la tabla. */
    public static final Long ID_UNICO = 1L;

    @Id
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tarifaBase;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioPorKm;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioPorKg;

    /** Multiplica la distancia en línea recta para aproximar la distancia por carretera. */
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal factorRuta;

    /** Distancia que se cobra como mínimo (envíos dentro de la misma ciudad). */
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal distanciaMinimaKm;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal pesoMaximoKg;

    /** Máximo para cada medida del paquete (largo, ancho y alto). */
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal medidaMaximaCm;

    /** Admin que hizo el último cambio. Null mientras nadie la haya modificado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por_id")
    private Usuario actualizadoPor;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    public ConfiguracionTarifa(BigDecimal tarifaBase, BigDecimal precioPorKm, BigDecimal precioPorKg,
                               BigDecimal factorRuta, BigDecimal distanciaMinimaKm, BigDecimal pesoMaximoKg,
                               BigDecimal medidaMaximaCm) {
        this.id = ID_UNICO;
        this.tarifaBase = tarifaBase;
        this.precioPorKm = precioPorKm;
        this.precioPorKg = precioPorKg;
        this.factorRuta = factorRuta;
        this.distanciaMinimaKm = distanciaMinimaKm;
        this.pesoMaximoKg = pesoMaximoKg;
        this.medidaMaximaCm = medidaMaximaCm;
    }

    @PrePersist
    @PreUpdate
    void alGuardar() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}

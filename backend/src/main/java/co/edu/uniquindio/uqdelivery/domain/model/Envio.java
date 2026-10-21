package co.edu.uniquindio.uqdelivery.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Solicitud de envío de un paquete (HU-04).
 * El costo y la distancia se calculan al crearlo y no se recalculan después.
 */
@Entity
@Table(name = "envios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código que usa el receptor para consultar su paquete, p. ej. UQD-7KQ2M9XA. */
    @Column(nullable = false, unique = true, length = 12)
    private String codigoSeguimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emisor_id", nullable = false)
    private Usuario emisor;

    /** Repartidor actual. Null hasta que se asigne uno (HU-06). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id")
    private Usuario repartidor;

    @Column(nullable = false, length = 200)
    private String direccionOrigen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Ciudad ciudadOrigen;

    @Column(nullable = false, length = 200)
    private String direccionDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Ciudad ciudadDestino;

    @Column(nullable = false, length = 100)
    private String nombreDestinatario;

    @Column(nullable = false, length = 20)
    private String telefonoDestinatario;

    /** Opcional. */
    @Column(length = 150)
    private String correoDestinatario;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal pesoKg;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal largoCm;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal anchoCm;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal altoCm;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal distanciaKm;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEnvio estado;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** Null hasta que se entregue. */
    private LocalDateTime fechaEntrega;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    public Envio(String codigoSeguimiento, Usuario emisor,
                 String direccionOrigen, Ciudad ciudadOrigen,
                 String direccionDestino, Ciudad ciudadDestino,
                 String nombreDestinatario, String telefonoDestinatario, String correoDestinatario,
                 String descripcion, BigDecimal pesoKg, BigDecimal largoCm, BigDecimal anchoCm, BigDecimal altoCm,
                 BigDecimal distanciaKm, BigDecimal costo) {
        this.codigoSeguimiento = codigoSeguimiento;
        this.emisor = emisor;
        this.direccionOrigen = direccionOrigen;
        this.ciudadOrigen = ciudadOrigen;
        this.direccionDestino = direccionDestino;
        this.ciudadDestino = ciudadDestino;
        this.nombreDestinatario = nombreDestinatario;
        this.telefonoDestinatario = telefonoDestinatario;
        this.correoDestinatario = correoDestinatario;
        this.descripcion = descripcion;
        this.pesoKg = pesoKg;
        this.largoCm = largoCm;
        this.anchoCm = anchoCm;
        this.altoCm = altoCm;
        this.distanciaKm = distanciaKm;
        this.costo = costo;
        this.estado = EstadoEnvio.PENDIENTE;
    }

    @PrePersist
    void alCrear() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = this.fechaCreacion;
    }

    @PreUpdate
    void alActualizar() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}

package co.edu.uniquindio.uqdelivery.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Enlace de recuperación de contraseña (HU-03). Vence a los 30 minutos y se usa una sola vez.
 */
@Entity
@Table(name = "tokens_recuperacion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenRecuperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private boolean usado;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public TokenRecuperacion(Usuario usuario, String token, LocalDateTime fechaExpiracion) {
        this.usuario = usuario;
        this.token = token;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = false;
    }

    @PrePersist
    void alCrear() {
        this.fechaCreacion = LocalDateTime.now();
    }
}

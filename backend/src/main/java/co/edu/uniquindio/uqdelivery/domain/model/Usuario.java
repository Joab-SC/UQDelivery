package co.edu.uniquindio.uqdelivery.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Cuenta de un usuario de la aplicación (admin, emisor o repartidor).
 * Solo guarda el hash de la contraseña, nunca el texto plano.
 */
@Entity
@Table(name = "usuarios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(nullable = false, length = 100)
    private String contrasenaHash;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo;

    @Column(nullable = false)
    private boolean notificacionesActivas;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    public Usuario(String nombre, String correo, String contrasenaHash, String telefono, Rol rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.telefono = telefono;
        this.rol = rol;
        this.activo = true;
        this.notificacionesActivas = true;
    }

    /**
     * HU-03: reemplaza la contraseña. Recibe el hash ya calculado (BCrypt) por el service:
     * la entidad nunca ve la contraseña en texto plano.
     */
    public void cambiarContrasena(String nuevoHash) {
        if (nuevoHash == null || nuevoHash.isBlank()) {
            throw new IllegalArgumentException("El hash de la contraseña no puede estar vacío");
        }
        this.contrasenaHash = nuevoHash;
    }

    /** Asigna un nuevo rol al usuario. */
    public void cambiarRol(Rol nuevoRol) {
        if (nuevoRol == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        this.rol = nuevoRol;
    }

    @PrePersist
    void alCrear() {
        this.fechaRegistro = LocalDateTime.now();
    }
}

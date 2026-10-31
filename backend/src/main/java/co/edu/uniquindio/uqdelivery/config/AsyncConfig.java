package co.edu.uniquindio.uqdelivery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Activa los métodos @Async: se ejecutan en otro hilo, sin hacer esperar la respuesta de la API.
 * Spring Boot ya trae configurado el grupo de hilos (applicationTaskExecutor).
 *
 * Lo agrega HU-03 para enviar el correo de recuperación en segundo plano. Las notificaciones del
 * Sprint 2 (HU-12, HU-13) pueden usarlo igual.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}

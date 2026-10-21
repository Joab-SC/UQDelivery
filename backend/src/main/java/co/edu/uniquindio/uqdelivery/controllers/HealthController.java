package co.edu.uniquindio.uqdelivery.controllers;

import co.edu.uniquindio.uqdelivery.dtos.response.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Permite comprobar que la API está arriba (útil para el despliegue y para el front).
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Estado de la API")
public class HealthController {

    @GetMapping
    @Operation(summary = "Comprobar que la API responde")
    @SecurityRequirements
    public HealthResponse health() {
        return new HealthResponse("UP");
    }
}

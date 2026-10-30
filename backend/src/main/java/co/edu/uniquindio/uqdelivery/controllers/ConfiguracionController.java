package co.edu.uniquindio.uqdelivery.controllers;

import co.edu.uniquindio.uqdelivery.config.mapper.EnvioMapper;
import co.edu.uniquindio.uqdelivery.dtos.response.PoliticasResponse;
import co.edu.uniquindio.uqdelivery.services.ConfiguracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Políticas del paquete (límites de peso y medidas). Cualquier usuario con sesión (HU-04). */
@RestController
@RequestMapping("/api/configuracion")
@RequiredArgsConstructor
@Tag(name = "Configuración", description = "Políticas de tamaño del paquete (HU-04)")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;
    private final EnvioMapper envioMapper;

    @GetMapping("/politicas")
    @Operation(summary = "Consultar el peso y las medidas máximas del paquete")
    public PoliticasResponse politicas() {
        return envioMapper.toPoliticasResponse(configuracionService.obtener());
    }
}
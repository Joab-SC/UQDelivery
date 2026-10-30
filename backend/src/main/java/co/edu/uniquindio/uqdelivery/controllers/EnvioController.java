package co.edu.uniquindio.uqdelivery.controllers;

import co.edu.uniquindio.uqdelivery.config.mapper.EnvioMapper;
import co.edu.uniquindio.uqdelivery.dtos.request.EnvioRequest;
import co.edu.uniquindio.uqdelivery.dtos.response.EnvioResponse;
import co.edu.uniquindio.uqdelivery.services.EnvioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/envios")
@RequiredArgsConstructor
@Tag(name = "Envíos", description = "Solicitudes de envío (HU-04)")
public class EnvioController {

    private final EnvioService envioService;
    private final EnvioMapper envioMapper;

    @PostMapping
    @PreAuthorize("hasRole('EMISOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una solicitud de envío: recalcula el costo y devuelve el código de seguimiento")
    public EnvioResponse crear(@Valid @RequestBody EnvioRequest request) {
        return envioMapper.toResponse(envioService.crear(envioMapper.toDatos(request)));
    }
}

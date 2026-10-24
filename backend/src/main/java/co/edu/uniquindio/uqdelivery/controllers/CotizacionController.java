package co.edu.uniquindio.uqdelivery.controllers;

import co.edu.uniquindio.uqdelivery.config.mapper.CotizacionMapper;
import co.edu.uniquindio.uqdelivery.dtos.request.CotizacionRequest;
import co.edu.uniquindio.uqdelivery.dtos.response.CotizacionResponse;
import co.edu.uniquindio.uqdelivery.services.CotizacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/envios")
@RequiredArgsConstructor
@Tag(name = "Cotización", description = "Cotización automática de envíos (HU-05)")
public class CotizacionController {

    private final CotizacionService cotizacionService;
    private final CotizacionMapper cotizacionMapper;

    @PostMapping("/cotizacion")
    @PreAuthorize("hasRole('EMISOR')")
    @Operation(summary = "Cotizar un envío: devuelve costo y distancia estimada")
    public CotizacionResponse cotizar(@Valid @RequestBody CotizacionRequest request) {
        return cotizacionMapper.toResponse(cotizacionService.cotizar(
                request.ciudadOrigen(), request.ciudadDestino(), request.pesoKg(),
                request.largoCm(), request.anchoCm(), request.altoCm()));
    }
}
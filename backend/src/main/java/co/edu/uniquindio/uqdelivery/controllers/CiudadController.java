package co.edu.uniquindio.uqdelivery.controllers;

import co.edu.uniquindio.uqdelivery.config.mapper.EnvioMapper;
import co.edu.uniquindio.uqdelivery.domain.model.Ciudad;
import co.edu.uniquindio.uqdelivery.dtos.response.CiudadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/** Lista de ciudades donde opera UQDelivery. Cualquier usuario con sesión (HU-04). */
@RestController
@RequestMapping("/api/ciudades")
@RequiredArgsConstructor
@Tag(name = "Ciudades", description = "Ciudades disponibles para los envíos (HU-04)")
public class CiudadController {

    private final EnvioMapper envioMapper;

    @GetMapping
    @Operation(summary = "Listar las ciudades donde opera UQDelivery")
    public List<CiudadResponse> listar() {
        return Arrays.stream(Ciudad.values()).map(envioMapper::toResponse).toList();
    }
}
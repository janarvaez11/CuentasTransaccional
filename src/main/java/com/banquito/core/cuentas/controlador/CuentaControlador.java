package com.banquito.core.cuentas.controlador;

import com.banquito.core.cuentas.dto.CuentaRespuestaDTO;
import com.banquito.core.cuentas.dto.CuentaSolicitudDTO;
import com.banquito.core.cuentas.servicio.CuentaServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cuentas")
@Tag(name = "Cuentas", description = "CRUD de cuentas bancarias")
@Slf4j
public class CuentaControlador {

  private final CuentaServicio servicio;

  public CuentaControlador(CuentaServicio servicio) {
    this.servicio = servicio;
  }

  @Operation(summary = "Listar todas las cuentas", description = "Devuelve todas las cuentas registradas")
  @ApiResponse(responseCode = "200", description = "Listado exitoso")
  @GetMapping
  public ResponseEntity<List<CuentaRespuestaDTO>> listarTodas() {
    log.info("GET /api/v1/cuentas - listar todas");
    return ResponseEntity.ok(servicio.listarTodas());
  }

  @Operation(summary = "Obtener cuenta por ID", description = "Devuelve los datos de una cuenta existente")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cuenta encontrada", content = @Content(schema = @Schema(implementation = CuentaRespuestaDTO.class))),
      @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
  })
  @GetMapping("/{id}")
  public ResponseEntity<CuentaRespuestaDTO> obtenerPorId(
      @Parameter(description = "ID de la cuenta", required = true) @PathVariable Integer id) {
    log.info("GET /api/v1/cuentas/{} - obtener cuenta", id);
    CuentaRespuestaDTO dto = servicio.obtener(id);
    return ResponseEntity.ok(dto);
  }

  @Operation(summary = "Crear nueva cuenta", description = "Registra una nueva cuenta bancaria")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Cuenta creada", content = @Content(schema = @Schema(implementation = CuentaRespuestaDTO.class))),
      @ApiResponse(responseCode = "400", description = "Solicitud inválida")
  })
  @PostMapping
  public ResponseEntity<CuentaRespuestaDTO> crear(
      @Parameter(description = "Datos para crear la cuenta", required = true) @Valid @RequestBody CuentaSolicitudDTO solicitud) {
    log.info("POST /api/v1/cuentas - crear cuenta código={}", solicitud.getCodigoCuenta());
    CuentaRespuestaDTO creado = servicio.crear(solicitud);
    return ResponseEntity.status(201).body(creado);
  }

  @Operation(summary = "Actualizar cuenta existente", description = "Modifica datos de una cuenta")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cuenta actualizada"),
      @ApiResponse(responseCode = "404", description = "Cuenta no encontrada"),
      @ApiResponse(responseCode = "400", description = "Solicitud inválida")
  })
  @PutMapping("/{id}")
  public ResponseEntity<CuentaRespuestaDTO> actualizar(
      @Parameter(description = "ID de la cuenta", required = true) @PathVariable Integer id,
      @Parameter(description = "Datos a actualizar", required = true) @Valid @RequestBody CuentaSolicitudDTO solicitud) {
    log.info("PUT /api/v1/cuentas/{} - actualizar cuenta", id);
    CuentaRespuestaDTO actualizado = servicio.actualizar(id, solicitud);
    return ResponseEntity.ok(actualizado);
  }

  @Operation(summary = "Eliminar cuenta (lógico)", description = "Marca la cuenta como eliminada")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Eliminación exitosa"),
      @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> eliminar(
      @Parameter(description = "ID de la cuenta", required = true) @PathVariable Integer id) {
    log.info("DELETE /api/v1/cuentas/{} - eliminar (lógico)", id);
    servicio.eliminar(id);
    return ResponseEntity.noContent().build();
  }
}

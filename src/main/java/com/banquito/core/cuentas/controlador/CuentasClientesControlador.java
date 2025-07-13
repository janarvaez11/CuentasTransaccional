package com.banquito.core.cuentas.controlador;

import com.banquito.core.cuentas.dto.CuentasClientesRespuestaDTO;
import com.banquito.core.cuentas.dto.CuentasClientesSolicitudDTO;

import com.banquito.core.cuentas.mapper.CuentasClientesMapper;
import com.banquito.core.cuentas.modelo.CuentasClientes;
import com.banquito.core.cuentas.servicio.CuentasClientesServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cuentas-clientes")
@Tag(name = "CuentasClientes", description = "Operaciones sobre cuentas de clientes")
@Slf4j
public class CuentasClientesControlador {

  private final CuentasClientesServicio service;

  public CuentasClientesControlador(CuentasClientesServicio service) {
    this.service = service;
  }

  @Operation(summary = "Obtener cuenta-cliente por ID", description = "Devuelve los datos de la cuenta-cliente especificada por su ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cuenta-cliente encontrada", content = @Content(schema = @Schema(implementation = CuentasClientesRespuestaDTO.class))),
      @ApiResponse(responseCode = "404", description = "No se encontró la cuenta-cliente")
  })
  @GetMapping("/{id}")
  public ResponseEntity<CuentasClientesRespuestaDTO> obtenerPorId(
      @Parameter(description = "ID de la cuenta-cliente", required = true) @PathVariable Integer id) {
    log.info("GET /api/v1/cuentas-clientes/{} - obtener por ID", id);
    CuentasClientes entidad = service.buscarPorId(id);
    return ResponseEntity.ok(CuentasClientesMapper.toCuentasClientesRespuestaDTO(entidad));
  }

  @Operation(summary = "Obtener cuenta-cliente por número de cuenta", description = "Devuelve los datos de la cuenta-cliente a partir de su número de cuenta")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cuenta-cliente encontrada", content = @Content(schema = @Schema(implementation = CuentasClientesRespuestaDTO.class))),
      @ApiResponse(responseCode = "404", description = "No se encontró la cuenta-cliente")
  })
  @GetMapping("/numero-cuenta/{numeroCuenta}")
  public ResponseEntity<CuentasClientesRespuestaDTO> obtenerPorNumeroCuenta(
      @Parameter(description = "Número de cuenta", required = true) @PathVariable String numeroCuenta) {
    log.info("GET /api/v1/cuentas-clientes/numero-cuenta/{} - obtener por número", numeroCuenta);
    CuentasClientes entidad = service.buscarPorNumeroCuenta(numeroCuenta);
    return ResponseEntity.ok(CuentasClientesMapper.toCuentasClientesRespuestaDTO(entidad));
  }

  @Operation(summary = "Obtener cuenta-cliente por cliente y número", description = "Devuelve la cuenta-cliente filtrando por ID de cliente y número de cuenta")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cuenta-cliente encontrada", content = @Content(schema = @Schema(implementation = CuentasClientesRespuestaDTO.class))),
      @ApiResponse(responseCode = "404", description = "No se encontró la cuenta-cliente")
  })
  @GetMapping("/cliente/{idCliente}/numero-cuenta/{numeroCuenta}")
  public ResponseEntity<CuentasClientesRespuestaDTO> obtenerPorClienteYNúmero(
      @Parameter(description = "ID del cliente", required = true) @PathVariable String idCliente,
      @Parameter(description = "Número de cuenta", required = true) @PathVariable String numeroCuenta) {
    log.info("GET /api/v1/cuentas-clientes/cliente/{}/numero-cuenta/{} - obtener por cliente y número",
        idCliente, numeroCuenta);
    CuentasClientes entidad = service.buscarPorIdClienteAndNumeroCuenta(idCliente, numeroCuenta);
    return ResponseEntity.ok(CuentasClientesMapper.toCuentasClientesRespuestaDTO(entidad));
  }

  @Operation(summary = "Crear nueva cuenta-cliente", description = "Registra una nueva relación cuenta-cliente")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Cuenta-cliente creada", content = @Content(schema = @Schema(implementation = CuentasClientesRespuestaDTO.class))),
      @ApiResponse(responseCode = "400", description = "Datos inválidos")
  })
  @PostMapping
  public ResponseEntity<CuentasClientesRespuestaDTO> crear(
      @Parameter(description = "Payload para crear la cuenta-cliente", required = true) @Valid @RequestBody CuentasClientesSolicitudDTO dto) {
    log.info("POST /api/v1/cuentas-clientes - crear para cliente {}", dto.getIdCliente());

    CuentasClientes entidad = CuentasClientesMapper.toCuentasClientes(dto);
    CuentasClientes creado = service.crearCuentasClientes(entidad);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(CuentasClientesMapper.toCuentasClientesRespuestaDTO(creado));
  }

  @Operation(summary = "Actualizar cuenta-cliente", description = "Modifica los datos de una cuenta-cliente existente")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cuenta-cliente actualizada", content = @Content(schema = @Schema(implementation = CuentasClientesRespuestaDTO.class))),
      @ApiResponse(responseCode = "404", description = "No se encontró la cuenta-cliente"),
      @ApiResponse(responseCode = "400", description = "Datos inválidos")
  })
  @PutMapping("/{id}")
  public ResponseEntity<CuentasClientesRespuestaDTO> actualizar(
      @Parameter(description = "ID de la cuenta-cliente", required = true) @PathVariable Integer id,
      @Parameter(description = "Payload con datos a actualizar", required = true) @Valid @RequestBody CuentasClientesSolicitudDTO dto) {
    log.info("PUT /api/v1/cuentas-clientes/{} - actualizar", id);
    CuentasClientes entidad = CuentasClientesMapper.toCuentasClientes(dto);
    CuentasClientes actualizado = service.actualizarCuentasClientes(id, entidad);
    return ResponseEntity.ok(CuentasClientesMapper.toCuentasClientesRespuestaDTO(actualizado));
  }

  @Operation(summary = "Desactivar cuenta-cliente", description = "Marca la cuenta-cliente como inactiva")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cuenta-cliente desactivada"),
      @ApiResponse(responseCode = "404", description = "No se encontró la cuenta-cliente")
  })
  @PutMapping("/{id}/desactivar")
  public ResponseEntity<CuentasClientesRespuestaDTO> desactivar(
      @Parameter(description = "ID de la cuenta-cliente", required = true) @PathVariable Integer id) {
    log.info("PUT /api/v1/cuentas-clientes/{}/desactivar", id);
    CuentasClientes desactivada = service.desactivarCuentasClientes(id);
    return ResponseEntity.ok(CuentasClientesMapper.toCuentasClientesRespuestaDTO(desactivada));
  }

  @Operation(summary = "Activar cuenta-cliente", description = "Marca la cuenta-cliente como activa")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cuenta-cliente activada"),
      @ApiResponse(responseCode = "404", description = "No se encontró la cuenta-cliente")
  })
  @PutMapping("/{id}/activar")
  public ResponseEntity<CuentasClientesRespuestaDTO> activar(
      @Parameter(description = "ID de la cuenta-cliente", required = true) @PathVariable Integer id) {
    log.info("PUT /api/v1/cuentas-clientes/{}/activar", id);
    CuentasClientes activada = service.activarCuentasClientes(id);
    return ResponseEntity.ok(CuentasClientesMapper.toCuentasClientesRespuestaDTO(activada));
  }
}

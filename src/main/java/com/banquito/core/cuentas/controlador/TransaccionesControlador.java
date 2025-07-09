package com.banquito.core.cuentas.controlador;

import com.banquito.core.cuentas.dto.TransaccionesSolicitudDTO;
import com.banquito.core.cuentas.dto.DesembolsoRespuestaDTO;
import com.banquito.core.cuentas.dto.DesembolsoSolicitudDTO;
import com.banquito.core.cuentas.dto.TransaccionesRespuestaDTO;
import com.banquito.core.cuentas.mapper.TransaccionesMapper;
import com.banquito.core.cuentas.modelo.Transacciones;
import com.banquito.core.cuentas.servicio.TransaccionesServicio;
import com.banquito.core.cuentas.enums.TipoTransaccionEnum;
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

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/transacciones")
@Tag(name = "Transacciones", description = "Operaciones de depósito, retiro y transferencia")
@Slf4j
public class TransaccionesControlador {

        private final TransaccionesServicio servicio;

        public TransaccionesControlador(TransaccionesServicio servicio) {
                this.servicio = servicio;
        }

        @Operation(summary = "Obtener transacción por ID", description = "Devuelve los datos de una transacción existente")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Transacción encontrada", content = @Content(schema = @Schema(implementation = TransaccionesRespuestaDTO.class))),
                        @ApiResponse(responseCode = "404", description = "No se encontró la transacción")
        })
        @GetMapping("/{id}")
        public ResponseEntity<TransaccionesRespuestaDTO> getById(
                        @Parameter(description = "ID de la transacción", required = true) @PathVariable Integer id) {
                log.info("GET /api/v1/transacciones/{} - obtener por ID", id);
                Transacciones t = servicio.buscarPorId(id);
                return ResponseEntity.ok(TransaccionesMapper.toDto(t));
        }

        @Operation(summary = "Listar transacciones por cuenta", description = "Devuelve todas las transacciones asociadas a una cuenta cliente")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Listado de transacciones", content = @Content(schema = @Schema(implementation = TransaccionesRespuestaDTO.class)))
        })
        @GetMapping("/por-cuenta/{idCuentaCliente}")
        public ResponseEntity<List<TransaccionesRespuestaDTO>> listarPorCuenta(
                        @Parameter(description = "ID de la cuenta cliente", required = true) @PathVariable Integer idCuentaCliente) {
                log.info("GET /api/v1/transacciones/por-cuenta/{} - listar", idCuentaCliente);
                List<Transacciones> lista = servicio.listarPorCuenta(idCuentaCliente);
                List<TransaccionesRespuestaDTO> dtos = lista.stream()
                                .map(TransaccionesMapper::toDto)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(dtos);
        }

        @Operation(summary = "Listar transacciones por rango de fechas", description = "Devuelve transacciones de una cuenta dentro de un rango de fechas ISO-8601")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Listado dentro del rango", content = @Content(schema = @Schema(implementation = TransaccionesRespuestaDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Formato de fecha inválido")
        })
        @GetMapping("/por-cuenta/{idCuentaCliente}/rango-fechas")
        public ResponseEntity<List<TransaccionesRespuestaDTO>> listarPorFechas(
                        @Parameter(description = "ID de la cuenta cliente", required = true) @PathVariable Integer idCuentaCliente,
                        @Parameter(description = "Fecha inicio en ISO-8601", required = true) @RequestParam String fechaInicio,
                        @Parameter(description = "Fecha fin en ISO-8601", required = true) @RequestParam String fechaFin) {
                log.info("GET /api/v1/transacciones/por-cuenta/{}/rango-fechas?fechaInicio={}&fechaFin={}",
                                idCuentaCliente, fechaInicio, fechaFin);
                Instant start = Instant.parse(fechaInicio);
                Instant end = Instant.parse(fechaFin);
                List<Transacciones> lista = servicio.listarPorCuentaYFechas(idCuentaCliente, start, end);
                List<TransaccionesRespuestaDTO> dtos = lista.stream()
                                .map(TransaccionesMapper::toDto)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(dtos);
        }

        @Operation(summary = "Realizar depósito", description = "Procesa un depósito en la cuenta especificada")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Depósito realizado", content = @Content(schema = @Schema(implementation = TransaccionesRespuestaDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Error en datos de entrada")
        })
        @PostMapping("/deposito")
        public ResponseEntity<TransaccionesRespuestaDTO> deposito(
                        @Parameter(description = "Datos para el depósito", required = true) @Valid @RequestBody TransaccionesSolicitudDTO dto) {
                log.info("POST /api/v1/transacciones/deposito");
                dto.setTipoTransaccion(TipoTransaccionEnum.DEPOSITO);
                Transacciones t = servicio.procesar(dto);
                return ResponseEntity.status(HttpStatus.CREATED).body(TransaccionesMapper.toDto(t));
        }

        @Operation(summary = "Realizar retiro", description = "Procesa un retiro de la cuenta especificada")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Retiro realizado", content = @Content(schema = @Schema(implementation = TransaccionesRespuestaDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Error en datos de entrada")
        })
        @PostMapping("/retiro")
        public ResponseEntity<TransaccionesRespuestaDTO> retiro(
                        @Parameter(description = "Datos para el retiro", required = true) @Valid @RequestBody TransaccionesSolicitudDTO dto) {
                log.info("POST /api/v1/transacciones/retiro");
                dto.setTipoTransaccion(TipoTransaccionEnum.RETIRO);
                Transacciones t = servicio.procesar(dto);
                return ResponseEntity.status(HttpStatus.CREATED).body(TransaccionesMapper.toDto(t));
        }

        @Operation(summary = "Realizar transferencia", description = "Procesa una transferencia entre dos cuentas")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Transferencia realizada", content = @Content(schema = @Schema(implementation = TransaccionesRespuestaDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Error en datos de entrada")
        })
        @PostMapping("/transferencia")
        public ResponseEntity<TransaccionesRespuestaDTO> transferencia(
                        @Parameter(description = "Datos para la transferencia", required = true) @Valid @RequestBody TransaccionesSolicitudDTO dto) {
                log.info("POST /api/v1/transacciones/transferencia");
                dto.setTipoTransaccion(TipoTransaccionEnum.TRANSFERENCIA);
                Transacciones t = servicio.procesar(dto);
                return ResponseEntity.status(HttpStatus.CREATED).body(TransaccionesMapper.toDto(t));
        }


        
        @Operation(summary = "Desembolsar préstamo", description = "Ejecuta retiro de la cuenta pool, depósito al cliente y transferencia a originación")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Desembolso procesado exitosamente", content = @Content(schema = @Schema(implementation = DesembolsoRespuestaDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                        @ApiResponse(responseCode = "404", description = "Alguna de las cuentas no fue encontrada"),
                        @ApiResponse(responseCode = "409", description = "Fondos insuficientes o cuenta inactiva")
        })
        @PostMapping("/desembolso")
        public ResponseEntity<DesembolsoRespuestaDTO> desembolso(
                        @Valid @RequestBody DesembolsoSolicitudDTO dto) {
                log.info("POST /api/v1/transacciones/desembolso → {}", dto);
                DesembolsoRespuestaDTO resultado = servicio.procesarDesembolso(dto);
                return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        }

}

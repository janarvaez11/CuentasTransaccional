package com.banquito.core.cuentas.controlador;

import com.banquito.core.cuentas.dto.TransaccionesSolicitudDTO;
import com.banquito.core.cuentas.dto.TransaccionRespuestaAsincronaDTO;
import com.banquito.core.cuentas.servicio.TransaccionesServicio;
import com.banquito.core.cuentas.producer.TransaccionesProducer;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cuentas/v1/transacciones")
@Tag(name = "Transacciones", description = "Validación y envío de transacciones a cola")
@Slf4j
public class TransaccionesControlador {

        private final TransaccionesServicio servicio;
        private final TransaccionesProducer producer;

        public TransaccionesControlador(
                        TransaccionesServicio servicio,
                        TransaccionesProducer producer) {
                this.servicio = servicio;
                this.producer = producer;
        }

        @Operation(summary = "Realizar depósito", description = "Valida y envía depósito a cola para procesamiento")
        @ApiResponses({
                        @ApiResponse(responseCode = "202", description = "Depósito enviado para procesamiento", content = @Content(schema = @Schema(implementation = TransaccionRespuestaAsincronaDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Error en datos de entrada o validación")
        })
        @PostMapping("/deposito")
        public ResponseEntity<TransaccionRespuestaAsincronaDTO> deposito(
                        @Parameter(description = "Datos para el depósito. Ejemplo: {\"numeroCuentaOrigen\":\"1234567890\",\"tipoTransaccion\":\"DEPOSITO\",\"monto\":100.00,\"descripcion\":\"Depósito ATM\"}", required = true) @Valid @RequestBody TransaccionesSolicitudDTO dto) {
                log.info("POST /api/cuentas/v1/transacciones/deposito - enviando a cola");
                dto.setTipoTransaccion(TipoTransaccionEnum.DEPOSITO);

                // Validar antes de enviar a cola
                servicio.validarTransaccion(dto);

                // Enviar a cola específica de depósitos
                producer.enviarDeposito(dto);

                TransaccionRespuestaAsincronaDTO response = TransaccionRespuestaAsincronaDTO.builder()
                                .mensaje("Depósito enviado para procesamiento")
                                .transaccionId(UUID.randomUUID().toString())
                                .estado("EN_COLA")
                                .tipoTransaccion("DEPOSITO")
                                .build();

                return ResponseEntity.accepted().body(response);
        }

        @Operation(summary = "Realizar retiro", description = "Valida y envía retiro a cola para procesamiento")
        @ApiResponses({
                        @ApiResponse(responseCode = "202", description = "Retiro enviado para procesamiento", content = @Content(schema = @Schema(implementation = TransaccionRespuestaAsincronaDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Error en datos de entrada o validación")
        })
        @PostMapping("/retiro")
        public ResponseEntity<TransaccionRespuestaAsincronaDTO> retiro(
                        @Parameter(description = "Datos para el retiro. Ejemplo: {\"numeroCuentaOrigen\":\"1234567890\",\"tipoTransaccion\":\"RETIRO\",\"monto\":50.00,\"descripcion\":\"Retiro ATM\"}", required = true) @Valid @RequestBody TransaccionesSolicitudDTO dto) {
                log.info("POST /api/cuentas/v1/transacciones/retiro - enviando a cola");
                dto.setTipoTransaccion(TipoTransaccionEnum.RETIRO);

                // Validar antes de enviar a cola
                servicio.validarTransaccion(dto);

                // Enviar a cola específica de retiros
                producer.enviarRetiro(dto);

                TransaccionRespuestaAsincronaDTO response = TransaccionRespuestaAsincronaDTO.builder()
                                .mensaje("Retiro enviado para procesamiento")
                                .transaccionId(UUID.randomUUID().toString())
                                .estado("EN_COLA")
                                .tipoTransaccion("RETIRO")
                                .build();

                return ResponseEntity.accepted().body(response);
        }

        @Operation(summary = "Realizar transferencia", description = "Valida y envía transferencia a cola para procesamiento")
        @ApiResponses({
                        @ApiResponse(responseCode = "202", description = "Transferencia enviada para procesamiento", content = @Content(schema = @Schema(implementation = TransaccionRespuestaAsincronaDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Error en datos de entrada o validación")
        })
        @PostMapping("/transferencia")
        public ResponseEntity<TransaccionRespuestaAsincronaDTO> transferencia(
                        @Parameter(description = "Datos para la transferencia. Ejemplo: {\"numeroCuentaOrigen\":\"1234567890\",\"numeroCuentaDestino\":\"0987654321\",\"tipoTransaccion\":\"TRANSFERENCIA\",\"monto\":25.00,\"descripcion\":\"Pago servicios\"}", required = true) @Valid @RequestBody TransaccionesSolicitudDTO dto) {
                log.info("POST /api/cuentas/v1/transacciones/transferencia - enviando a cola");
                dto.setTipoTransaccion(TipoTransaccionEnum.TRANSFERENCIA);

                // Validar antes de enviar a cola
                servicio.validarTransaccion(dto);

                // Enviar a cola específica de transferencias
                producer.enviarTransferencia(dto);

                TransaccionRespuestaAsincronaDTO response = TransaccionRespuestaAsincronaDTO.builder()
                                .mensaje("Transferencia enviada para procesamiento")
                                .transaccionId(UUID.randomUUID().toString())
                                .estado("EN_COLA")
                                .tipoTransaccion("TRANSFERENCIA")
                                .build();

                return ResponseEntity.accepted().body(response);
        }

        /**
         * NUEVO ENDPOINT UNIFICADO: Procesa cualquier tipo de transacción
         * El MS2 usará el campo tipoTransaccion para determinar qué operación realizar
         */
        @Operation(summary = "Procesar transacción unificada", description = "Valida y envía cualquier tipo de transacción a cola para procesamiento")
        @ApiResponses({
                        @ApiResponse(responseCode = "202", description = "Transacción enviada para procesamiento", content = @Content(schema = @Schema(implementation = TransaccionRespuestaAsincronaDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Error en datos de entrada o validación")
        })
        @PostMapping("/procesar")
        public ResponseEntity<TransaccionRespuestaAsincronaDTO> procesarTransaccion(
                        @Parameter(description = "Datos de la transacción (el tipoTransaccion determina la operación). Para transferencias incluir numeroCuentaDestino", required = true) @Valid @RequestBody TransaccionesSolicitudDTO dto) {
                log.info("POST /api/cuentas/v1/transacciones/procesar - tipo: {} - enviando a cola", dto.getTipoTransaccion());

                // Validar antes de enviar a cola
                servicio.validarTransaccion(dto);

                // Enviar a cola
                producer.enviarTransaccion(dto);

                TransaccionRespuestaAsincronaDTO response = TransaccionRespuestaAsincronaDTO.builder()
                                .mensaje(dto.getTipoTransaccion() + " enviado para procesamiento")
                                .transaccionId(UUID.randomUUID().toString())
                                .estado("EN_COLA")
                                .tipoTransaccion(dto.getTipoTransaccion().toString())
                                .build();

                return ResponseEntity.accepted().body(response);
        }
}

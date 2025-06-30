package com.banquito.core.cuentas.controlador;

import com.banquito.core.cuentas.dto.TransaccionesSolicitudDTO;
import com.banquito.core.cuentas.dto.TransaccionesRespuestaDTO;
import com.banquito.core.cuentas.excepcion.CrearEntidadExcepcion;
import com.banquito.core.cuentas.excepcion.EntidadNoEncontradaExcepcion;
import com.banquito.core.cuentas.mapper.TransaccionesMapper;
import com.banquito.core.cuentas.modelo.Transacciones;
import com.banquito.core.cuentas.servicio.TransaccionesServicio;
import com.banquito.core.cuentas.enums.TipoTransaccionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/transacciones")
@Slf4j
public class TransaccionesControlador {

    private final TransaccionesServicio servicio;

    public TransaccionesControlador(TransaccionesServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransaccionesRespuestaDTO> getById(@PathVariable Integer id) {
        log.info("GET /transacciones/{}", id);
        try {
            Transacciones t = servicio.buscarPorId(id);
            return ResponseEntity.ok(TransaccionesMapper.toDto(t));
        } catch (EntidadNoEncontradaExcepcion e) {
            log.warn("No encontrada transacción ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error al obtener transacción ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/por-cuenta/{idCuentaCliente}")
    public ResponseEntity<List<TransaccionesRespuestaDTO>> getByCuenta(@PathVariable Integer idCuentaCliente) {
        log.info("GET /transacciones/por-cuenta/{}", idCuentaCliente);
        try {
            List<Transacciones> lista = servicio.listarPorCuenta(idCuentaCliente);
            List<TransaccionesRespuestaDTO> dtos = lista.stream()
                    .map(TransaccionesMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error al listar transacciones por cuenta {}: {}", idCuentaCliente, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/por-cuenta/{idCuentaCliente}/rango-fechas")
    public ResponseEntity<List<TransaccionesRespuestaDTO>> getByCuentaAndFechas(
            @PathVariable Integer idCuentaCliente,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        log.info("GET /transacciones/por-cuenta/{}/rango-fechas?fechaInicio={}&fechaFin={}",
                idCuentaCliente, fechaInicio, fechaFin);
        try {
            Instant start = Instant.parse(fechaInicio);
            Instant end = Instant.parse(fechaFin);
            List<Transacciones> lista = servicio.listarPorCuentaYFechas(idCuentaCliente, start, end);
            List<TransaccionesRespuestaDTO> dtos = lista.stream()
                    .map(TransaccionesMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (DateTimeParseException e) {
            log.warn("Fechas inválidas: {} - {}", fechaInicio, fechaFin);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error al listar transacciones por rango: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/deposito")
    public ResponseEntity<TransaccionesRespuestaDTO> deposito(
            @Valid @RequestBody TransaccionesSolicitudDTO dto) {
        log.info("POST /transacciones/deposito -> cuentaClienteOrigen={}", dto.getIdCuentaClienteOrigen());
        try {
            dto.setTipoTransaccion(TipoTransaccionEnum.DEPOSITO);
            Transacciones t = servicio.procesar(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(TransaccionesMapper.toDto(t));
        } catch (CrearEntidadExcepcion | EntidadNoEncontradaExcepcion e) {
            log.warn("Error en depósito: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error inesperado depósito: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/retiro")
    public ResponseEntity<TransaccionesRespuestaDTO> retiro(
            @Valid @RequestBody TransaccionesSolicitudDTO dto) {
        log.info("POST /transacciones/retiro -> cuentaClienteOrigen={}", dto.getIdCuentaClienteOrigen());
        try {
            dto.setTipoTransaccion(TipoTransaccionEnum.RETIRO);
            Transacciones t = servicio.procesar(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(TransaccionesMapper.toDto(t));
        } catch (CrearEntidadExcepcion | EntidadNoEncontradaExcepcion e) {
            log.warn("Error en retiro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error inesperado retiro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/transferencia")
    public ResponseEntity<TransaccionesRespuestaDTO> transferencia(
            @Valid @RequestBody TransaccionesSolicitudDTO dto) {
        log.info("POST /transacciones/transferencia -> {} -> {}",
                dto.getIdCuentaClienteOrigen(), dto.getIdCuentaClienteDestino());
        try {
            dto.setTipoTransaccion(TipoTransaccionEnum.TRANSFERENCIA);
            Transacciones t = servicio.procesar(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(TransaccionesMapper.toDto(t));
        } catch (CrearEntidadExcepcion | EntidadNoEncontradaExcepcion e) {
            log.warn("Error en transferencia: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error inesperado transferencia: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

package com.banquito.core.cuentas.controlador;

import com.banquito.core.cuentas.dto.CuentaRespuestaDTO;
import com.banquito.core.cuentas.dto.CuentaSolicitudDTO;
import com.banquito.core.cuentas.excepcion.ActualizarEntidadExcepcion;
import com.banquito.core.cuentas.excepcion.CrearEntidadExcepcion;
import com.banquito.core.cuentas.excepcion.EliminarEntidadExcepcion;
import com.banquito.core.cuentas.excepcion.EntidadNoEncontradaExcepcion;
import com.banquito.core.cuentas.servicio.CuentaServicio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cuentas")
@Slf4j
public class CuentaControlador {

    private final CuentaServicio cuentaServicio;

    public CuentaControlador(CuentaServicio cuentaServicio) {
        this.cuentaServicio = cuentaServicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaRespuestaDTO> obtenerPorId(@PathVariable Integer id) {
        log.info("GET /api/v1/cuentas/{} - obtener cuenta", id);
        try {
            CuentaRespuestaDTO dto = cuentaServicio.obtener(id);
            return ResponseEntity.ok(dto);
        } catch (EntidadNoEncontradaExcepcion e) {
            log.warn("Cuenta no encontrada ID={}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error inesperado GET /api/v1/cuentas/{}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<CuentaRespuestaDTO> crear(@RequestBody CuentaSolicitudDTO solicitud) {
        log.info("POST /api/v1/cuentas - crear cuenta código={}", solicitud.getCodigoCuenta());
        try {
            CuentaRespuestaDTO creado = cuentaServicio.crear(solicitud);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (CrearEntidadExcepcion e) {
            log.error("Error al crear cuenta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error inesperado POST /api/v1/cuentas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaRespuestaDTO> actualizar(
            @PathVariable Integer id,
            @RequestBody CuentaSolicitudDTO solicitud
    ) {
        log.info("PUT /api/v1/cuentas/{} - actualizar cuenta", id);
        try {
            CuentaRespuestaDTO actualizado = cuentaServicio.actualizar(id, solicitud);
            return ResponseEntity.ok(actualizado);
        } catch (EntidadNoEncontradaExcepcion e) {
            log.warn("Cuenta no encontrada para actualizar ID={}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ActualizarEntidadExcepcion e) {
            log.error("Error al actualizar cuenta ID={}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error inesperado PUT /api/v1/cuentas/{}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        log.info("DELETE /api/v1/cuentas/{} - eliminar (lógico)", id);
        try {
            cuentaServicio.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (EntidadNoEncontradaExcepcion e) {
            log.warn("Cuenta no encontrada para eliminar ID={}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (EliminarEntidadExcepcion e) {
            log.error("Error al eliminar cuenta ID={}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error inesperado DELETE /api/v1/cuentas/{}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

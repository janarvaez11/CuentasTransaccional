package com.banquito.core.cuentas.servicio;

import com.banquito.core.cuentas.dto.TransaccionesSolicitudDTO;
import com.banquito.core.cuentas.enums.EstadoCuentaClienteEnum;
import com.banquito.core.cuentas.excepcion.CrearEntidadExcepcion;
import com.banquito.core.cuentas.excepcion.EntidadNoEncontradaExcepcion;
import com.banquito.core.cuentas.modelo.CuentasClientes;
import com.banquito.core.cuentas.repositorio.CuentasClientesRepositorio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
public class TransaccionesServicio {

    private final CuentasClientesRepositorio cliRepo;

    public TransaccionesServicio(CuentasClientesRepositorio cliRepo) {
        this.cliRepo = cliRepo;
    }

    @Transactional(readOnly = true)
    public void validarTransaccion(TransaccionesSolicitudDTO dto) {
        log.info("Validando transacción tipo: {} para cuenta: {}", 
                dto.getTipoTransaccion(), dto.getNumeroCuentaOrigen());

        switch (dto.getTipoTransaccion()) {
            case DEPOSITO:
                validarDeposito(dto);
                break;
            case RETIRO:
                validarRetiro(dto);
                break;
            case TRANSFERENCIA:
                validarTransferenciaCompleta(dto);
                break;
            default:
                throw new CrearEntidadExcepcion(
                        "Transacciones", "Tipo de transacción inválido: " + dto.getTipoTransaccion());
        }
        log.info("Validación exitosa para transacción tipo: {}", dto.getTipoTransaccion());
    }

    private CuentasClientes getCuentaPorNumero(String numeroCuenta) {
        log.debug("Buscando cuenta con número: {}", numeroCuenta);
        return cliRepo.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new EntidadNoEncontradaExcepcion(
                        "Cuenta", "Número de cuenta " + numeroCuenta + " no encontrada"));
    }

    private void validarActiva(CuentasClientes cc) {
        if (cc.getEstado() != EstadoCuentaClienteEnum.ACTIVO) {
            throw new CrearEntidadExcepcion(
                    "Cuenta", "La cuenta " + cc.getNumeroCuenta() + " no está activa. Estado actual: " + cc.getEstado());
        }
    }

    // ========== MÉTODOS DE VALIDACIÓN PARA MS1 ==========

    private void validarDeposito(TransaccionesSolicitudDTO dto) {
        // Validar que la cuenta existe y esté activa
        CuentasClientes cuenta = getCuentaPorNumero(dto.getNumeroCuentaOrigen());
        validarActiva(cuenta);

        // Validar monto positivo
        if (dto.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CrearEntidadExcepcion("Monto", "El monto debe ser mayor que cero. Monto recibido: " + dto.getMonto());
        }

        log.debug("Validación de depósito exitosa para cuenta: {}", dto.getNumeroCuentaOrigen());
    }

    private void validarRetiro(TransaccionesSolicitudDTO dto) {
        // Validar que la cuenta existe y esté activa
        CuentasClientes cuenta = getCuentaPorNumero(dto.getNumeroCuentaOrigen());
        validarActiva(cuenta);

        // Validar monto positivo
        if (dto.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CrearEntidadExcepcion("Monto", "El monto debe ser mayor que cero. Monto recibido: " + dto.getMonto());
        }

        // Validar saldo suficiente
        if (cuenta.getSaldoDisponible().compareTo(dto.getMonto()) < 0) {
            throw new CrearEntidadExcepcion("Saldo", 
                "Saldo insuficiente en cuenta " + dto.getNumeroCuentaOrigen() + 
                ". Saldo disponible: $" + cuenta.getSaldoDisponible() + 
                ", Monto solicitado: $" + dto.getMonto());
        }

        log.debug("Validación de retiro exitosa para cuenta: {}", dto.getNumeroCuentaOrigen());
    }

    private void validarTransferenciaCompleta(TransaccionesSolicitudDTO dto) {
        // Validar que se proporcionó cuenta destino
        if (dto.getNumeroCuentaDestino() == null || dto.getNumeroCuentaDestino().trim().isEmpty()) {
            throw new CrearEntidadExcepcion("Transferencia", "Para transferencias es obligatorio especificar el número de cuenta destino");
        }

        // Validar cuentas diferentes
        if (dto.getNumeroCuentaOrigen().equals(dto.getNumeroCuentaDestino())) {
            throw new CrearEntidadExcepcion("Transferencia", "La cuenta origen y destino no pueden ser la misma");
        }

        // Validar cuenta origen
        CuentasClientes origen = getCuentaPorNumero(dto.getNumeroCuentaOrigen());
        validarActiva(origen);

        // Validar cuenta destino
        CuentasClientes destino = getCuentaPorNumero(dto.getNumeroCuentaDestino());
        validarActiva(destino);

        // Validar monto positivo
        if (dto.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CrearEntidadExcepcion("Monto", "El monto debe ser mayor que cero. Monto recibido: " + dto.getMonto());
        }

        // Validar saldo suficiente en origen
        if (origen.getSaldoDisponible().compareTo(dto.getMonto()) < 0) {
            throw new CrearEntidadExcepcion("Saldo", 
                "Saldo insuficiente en cuenta origen " + dto.getNumeroCuentaOrigen() + 
                ". Saldo disponible: $" + origen.getSaldoDisponible() + 
                ", Monto solicitado: $" + dto.getMonto());
        }

        log.debug("Validación de transferencia exitosa: {} -> {}",
                dto.getNumeroCuentaOrigen(), dto.getNumeroCuentaDestino());
    }

}
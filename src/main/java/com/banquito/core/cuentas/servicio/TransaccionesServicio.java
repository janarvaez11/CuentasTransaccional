package com.banquito.core.cuentas.servicio;

import com.banquito.core.cuentas.dto.DesembolsoRespuestaDTO;
import com.banquito.core.cuentas.dto.DesembolsoSolicitudDTO;
import com.banquito.core.cuentas.dto.TransaccionesRespuestaDTO;
import com.banquito.core.cuentas.dto.TransaccionesSolicitudDTO;
import com.banquito.core.cuentas.enums.EstadoCuentaClienteEnum;
import com.banquito.core.cuentas.enums.EstadoTransaccionesEnum;
import com.banquito.core.cuentas.enums.TipoTransaccionEnum;
import com.banquito.core.cuentas.excepcion.CrearEntidadExcepcion;
import com.banquito.core.cuentas.excepcion.EntidadNoEncontradaExcepcion;
import com.banquito.core.cuentas.mapper.TransaccionesMapper;
import com.banquito.core.cuentas.modelo.CuentasClientes;
import com.banquito.core.cuentas.modelo.Transacciones;
import com.banquito.core.cuentas.repositorio.CuentasClientesRepositorio;
import com.banquito.core.cuentas.repositorio.TransaccionesRepositorio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class TransaccionesServicio {

    private final TransaccionesRepositorio transRepo;
    private final CuentasClientesRepositorio cliRepo;
    private final Integer poolAccountId;

    public TransaccionesServicio(
            TransaccionesRepositorio transRepo,
            CuentasClientesRepositorio cliRepo,
            @Value("${cuentas.desembolso.pool-account-id}") Integer poolAccountId) {
        this.transRepo = transRepo;
        this.cliRepo = cliRepo;
        this.poolAccountId  = poolAccountId;
    }

    @Transactional(readOnly = true)
    public Transacciones buscarPorId(Integer id) {
        return transRepo.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaExcepcion(
                        "Transacciones", "ID " + id + " no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<Transacciones> listarPorCuenta(Integer idCliente) {
        return transRepo.findByIdCuentaCliente_IdOrderByFechaTransaccionDesc(idCliente);
    }

    @Transactional(readOnly = true)
    public List<Transacciones> listarPorCuentaYFechas(
            Integer idCliente, Instant from, Instant to) {
        return transRepo
                .findByIdCuentaCliente_IdAndFechaTransaccionBetweenOrderByFechaTransaccionDesc(
                        idCliente, from, to);
    }

    @Transactional
    public Transacciones procesar(TransaccionesSolicitudDTO dto) {
        Transacciones t = TransaccionesMapper.toEntity(dto);
        t.setFechaTransaccion(Instant.now());

        switch (t.getTipoTransaccion()) {
            case DEPOSITO:
                return procesarDeposito(t);
            case RETIRO:
                return procesarRetiro(t);
            case TRANSFERENCIA:
                return procesarTransferencia(t);
            default:
                throw new CrearEntidadExcepcion(
                        "Transacciones", "Tipo inválido: " + t.getTipoTransaccion());
        }
    }

    private Transacciones procesarDeposito(Transacciones t) {
        if (t.getTipoTransaccion() != TipoTransaccionEnum.DEPOSITO) {
            throw new CrearEntidadExcepcion(
                    "Transacciones", "No es DEPÓSITO");
        }
        CuentasClientes cc = cliRepo.findById(
                t.getIdCuentaCliente().getId()).orElseThrow(
                        () -> new EntidadNoEncontradaExcepcion(
                                "CuentasClientes", "ID " + t.getIdCuentaCliente().getId() + " no encontrada"));

        t.setIdCuentaCliente(cc);

        if (cc.getVersion() == null) {
            cc.setVersion(0L);
        }
        validarActiva(cc);
        cc.setSaldoDisponible(cc.getSaldoDisponible().add(t.getMonto()));
        cc.setSaldoContable(cc.getSaldoContable().add(t.getMonto()));
        cliRepo.save(cc);

        t.setEstado(EstadoTransaccionesEnum.PROCESADO);
        return transRepo.save(t);
    }

    private Transacciones procesarRetiro(Transacciones t) {
        if (t.getTipoTransaccion() != TipoTransaccionEnum.RETIRO) {
            throw new CrearEntidadExcepcion(
                    "Transacciones", "No es RETIRO");
        }
        CuentasClientes cc = cliRepo.findById(
                t.getIdCuentaCliente().getId()).orElseThrow(
                        () -> new EntidadNoEncontradaExcepcion(
                                "CuentasClientes", "ID " + t.getIdCuentaCliente().getId() + " no encontrada"));
        t.setIdCuentaCliente(cc);

        if (cc.getVersion() == null) {
            cc.setVersion(0L);
        }
        validarActiva(cc);
        if (cc.getSaldoDisponible().compareTo(t.getMonto()) < 0) {
            throw new CrearEntidadExcepcion(
                    "Transacciones", "Saldo insuficiente");
        }
        cc.setSaldoDisponible(cc.getSaldoDisponible().subtract(t.getMonto()));
        cc.setSaldoContable(cc.getSaldoContable().subtract(t.getMonto()));
        cliRepo.save(cc);
        t.setVersion(0L);
        t.setEstado(EstadoTransaccionesEnum.PROCESADO);
        return transRepo.save(t);
    }

    private Transacciones procesarTransferencia(Transacciones t) {
        // origen
        validarTransferencia(t);
        CuentasClientes origen = cliRepo.findById(
                t.getIdCuentaCliente().getId()).orElseThrow(
                        () -> new EntidadNoEncontradaExcepcion(
                                "CuentasClientes", "Origen ID " + t.getIdCuentaCliente().getId() + " no encontrada"));
        CuentasClientes destino = cliRepo.findById(
                t.getIdCuentaClienteDestino()).orElseThrow(
                        () -> new EntidadNoEncontradaExcepcion(
                                "CuentasClientes", "Destino ID " + t.getIdCuentaClienteDestino() + " no encontrada"));

        if (origen.getVersion() == null)
            origen.setVersion(0L);
        if (destino.getVersion() == null)
            destino.setVersion(0L);

        validarActiva(origen);
        validarActiva(destino);
        if (origen.getSaldoDisponible().compareTo(t.getMonto()) < 0) {
            throw new CrearEntidadExcepcion(
                    "Transacciones", "Saldo insuficiente en origen");
        }
        // débito origen
        Transacciones debito = cloneForTransfer(origen, t.getMonto().negate(),
                "Transf saliente: " + t.getDescripcion());
        // crédito destino
        Transacciones credito = cloneForTransfer(destino, t.getMonto(),
                "Transf entrante: " + t.getDescripcion());

        // aplicar cambios saldos
        origen.setSaldoDisponible(origen.getSaldoDisponible().subtract(t.getMonto()));
        origen.setSaldoContable(origen.getSaldoContable().subtract(t.getMonto()));
        destino.setSaldoDisponible(destino.getSaldoDisponible().add(t.getMonto()));
        destino.setSaldoContable(destino.getSaldoContable().add(t.getMonto()));
        cliRepo.save(origen);
        cliRepo.save(destino);

        transRepo.save(credito);
        return transRepo.save(debito);
    }

    private void validarActiva(CuentasClientes cc) {
        if (cc.getEstado() != EstadoCuentaClienteEnum.ACTIVO) {
            throw new CrearEntidadExcepcion(
                    "Transacciones", "Cuenta inactiva");
        }
    }

    private void validarTransferencia(Transacciones t) {
        if (t.getIdCuentaCliente().getId().equals(t.getIdCuentaClienteDestino())) {
            throw new CrearEntidadExcepcion(
                    "Transacciones", "Mismo origen y destino");
        }
        if (t.getTipoTransaccion() != TipoTransaccionEnum.TRANSFERENCIA) {
            throw new CrearEntidadExcepcion(
                    "Transacciones", "No es TRANSFERENCIA");
        }
    }

    private Transacciones cloneForTransfer(
            CuentasClientes cc, BigDecimal monto, String desc) {
        Transacciones tx = new Transacciones();
        tx.setIdCuentaCliente(cc);
        tx.setIdCuentaClienteDestino(cc.getId()); // no se usa en saldos, pero llenamos
        tx.setTipoTransaccion(TipoTransaccionEnum.TRANSFERENCIA);
        tx.setMonto(monto);
        tx.setDescripcion(desc);
        tx.setFechaTransaccion(Instant.now());
        tx.setEstado(EstadoTransaccionesEnum.PROCESADO);
        tx.setVersion(0L); // nuevo registro, versión inicial
        return tx;
    }


    //Proceos de desembolso

@Transactional
public DesembolsoRespuestaDTO procesarDesembolso(DesembolsoSolicitudDTO dto) {
    // 1) Cargo y valido las 3 cuentas
    CuentasClientes pool   = getCuenta(poolAccountId);
    CuentasClientes cliente= getCuenta(dto.getIdCuentaCliente());
    CuentasClientes origin = getCuenta(dto.getIdCuentaOriginacion());

    validarActiva(pool);
    validarActiva(cliente);
    validarActiva(origin);

    // 2) Genero DTOs de cada paso
    TransaccionesSolicitudDTO retiroPoolDto = TransaccionesSolicitudDTO.builder()
        .idCuentaClienteOrigen(pool.getId())
        .tipoTransaccion(TipoTransaccionEnum.RETIRO)
        .monto(dto.getMonto())
        .descripcion("Desembolso a cliente " + cliente.getId())
        .build();

    TransaccionesSolicitudDTO depositoClienteDto = TransaccionesSolicitudDTO.builder()
        .idCuentaClienteOrigen(cliente.getId())
        .tipoTransaccion(TipoTransaccionEnum.DEPOSITO)
        .monto(dto.getMonto())
        .descripcion(dto.getDescripcion())
        .build();

    TransaccionesSolicitudDTO transferenciaOriginDto = TransaccionesSolicitudDTO.builder()
        .idCuentaClienteOrigen(cliente.getId())
        .idCuentaClienteDestino(origin.getId())
        .tipoTransaccion(TipoTransaccionEnum.TRANSFERENCIA)
        .monto(dto.getMonto())
        .descripcion("Pago concesionaria préstamo")
        .build();

    // 3) Invoco tu método procesar(...) directamente
    Transacciones retiroPoolTx      = procesar(retiroPoolDto);
    Transacciones depositoClienteTx = procesar(depositoClienteDto);
    Transacciones transferOriginTx  = procesar(transferenciaOriginDto);

    log.info("Desembolso completo: poolTx={}, depClienteTx={}, transfOriginTx={}",
        retiroPoolTx.getId(),
        depositoClienteTx.getId(),
        transferOriginTx.getId());

    return DesembolsoRespuestaDTO.builder()
        .retiroPool(TransaccionesMapper.toDto(retiroPoolTx))
        .depositoCliente(TransaccionesMapper.toDto(depositoClienteTx))
        .transferenciaOrigen(TransaccionesMapper.toDto(transferOriginTx))
        .build();
}

private CuentasClientes getCuenta(Integer id) {
    return cliRepo.findById(id)
      .orElseThrow(() -> new EntidadNoEncontradaExcepcion(
         "CuentasClientes", "ID " + id + " no encontrada"));
}




}
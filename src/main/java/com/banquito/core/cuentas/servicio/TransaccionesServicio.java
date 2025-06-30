package com.banquito.core.cuentas.servicio;

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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class TransaccionesServicio {

    private final TransaccionesRepositorio transRepo;
    private final CuentasClientesRepositorio cliRepo;

    public TransaccionesServicio(
        TransaccionesRepositorio transRepo,
        CuentasClientesRepositorio cliRepo
    ) {
        this.transRepo = transRepo;
        this.cliRepo   = cliRepo;
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
        Integer idCliente, Instant from, Instant to
    ) {
        return transRepo
            .findByIdCuentaCliente_IdAndFechaTransaccionBetweenOrderByFechaTransaccionDesc(
                idCliente, from, to
            );
    }

    @Transactional
    public Transacciones procesar(TransaccionesSolicitudDTO dto) {
        Transacciones t = TransaccionesMapper.toEntity(dto);
        t.setFechaTransaccion(Instant.now());
        t.setVersion(0L);

        switch (t.getTipoTransaccion()) {
            case DEPOSITO:
                return procesarDeposito(t);
            case RETIRO:
                return procesarRetiro(t);
            case TRANSFERENCIA:
                return procesarTransferencia(t);
            default:
                throw new CrearEntidadExcepcion(
                    "Transacciones", "Tipo inválido: " + t.getTipoTransaccion()
                );
        }
    }

    private Transacciones procesarDeposito(Transacciones t) {
        if (t.getTipoTransaccion() != TipoTransaccionEnum.DEPOSITO) {
            throw new CrearEntidadExcepcion(
                "Transacciones", "No es DEPÓSITO"
            );
        }
        CuentasClientes cc = cliRepo.findById(
            t.getIdCuentaCliente().getId()
        ).orElseThrow(() -> new EntidadNoEncontradaExcepcion(
            "CuentasClientes", "ID " + t.getIdCuentaCliente().getId() + " no encontrada"
        ));
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
                "Transacciones", "No es RETIRO"
            );
        }
        CuentasClientes cc = cliRepo.findById(
            t.getIdCuentaCliente().getId()
        ).orElseThrow(() -> new EntidadNoEncontradaExcepcion(
            "CuentasClientes", "ID " + t.getIdCuentaCliente().getId() + " no encontrada"
        ));
        validarActiva(cc);
        if (cc.getSaldoDisponible().compareTo(t.getMonto()) < 0) {
            throw new CrearEntidadExcepcion(
                "Transacciones", "Saldo insuficiente"
            );
        }
        cc.setSaldoDisponible(cc.getSaldoDisponible().subtract(t.getMonto()));
        cc.setSaldoContable(cc.getSaldoContable().subtract(t.getMonto()));
        cliRepo.save(cc);

        t.setEstado(EstadoTransaccionesEnum.PROCESADO);
        return transRepo.save(t);
    }

    private Transacciones procesarTransferencia(Transacciones t) {
        // origen
        validarTransferencia(t);
        CuentasClientes origen = cliRepo.findById(
            t.getIdCuentaCliente().getId()
        ).orElseThrow(() -> new EntidadNoEncontradaExcepcion(
            "CuentasClientes", "Origen ID " + t.getIdCuentaCliente().getId() + " no encontrada"
        ));
        CuentasClientes destino = cliRepo.findById(
            t.getIdCuentaClienteDestino()
        ).orElseThrow(() -> new EntidadNoEncontradaExcepcion(
            "CuentasClientes", "Destino ID " + t.getIdCuentaClienteDestino() + " no encontrada"
        ));
        validarActiva(origen);
        validarActiva(destino);
        if (origen.getSaldoDisponible().compareTo(t.getMonto()) < 0) {
            throw new CrearEntidadExcepcion(
                "Transacciones", "Saldo insuficiente en origen"
            );
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
                "Transacciones", "Cuenta inactiva"
            );
        }
    }

    private void validarTransferencia(Transacciones t) {
        if (t.getIdCuentaCliente().getId().equals(t.getIdCuentaClienteDestino())) {
            throw new CrearEntidadExcepcion(
                "Transacciones", "Mismo origen y destino"
            );
        }
        if (t.getTipoTransaccion() != TipoTransaccionEnum.TRANSFERENCIA) {
            throw new CrearEntidadExcepcion(
                "Transacciones", "No es TRANSFERENCIA"
            );
        }
    }

    private Transacciones cloneForTransfer(
        CuentasClientes cc, BigDecimal monto, String desc
    ) {
        Transacciones tx = new Transacciones();
        tx.setIdCuentaCliente(cc);
        tx.setIdCuentaClienteDestino(cc.getId()); // no se usa en saldos, pero llenamos
        tx.setTipoTransaccion(TipoTransaccionEnum.TRANSFERENCIA);
        tx.setMonto(monto);
        tx.setDescripcion(desc);
        tx.setFechaTransaccion(Instant.now());
        tx.setEstado(EstadoTransaccionesEnum.PROCESADO);
        tx.setVersion(0L);
        return tx;
    }
}
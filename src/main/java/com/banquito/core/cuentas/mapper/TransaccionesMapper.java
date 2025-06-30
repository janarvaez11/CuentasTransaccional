package com.banquito.core.cuentas.mapper;

import com.banquito.core.cuentas.dto.TransaccionesRespuestaDTO;
import com.banquito.core.cuentas.dto.TransaccionesSolicitudDTO;
import com.banquito.core.cuentas.modelo.CuentasClientes;
import com.banquito.core.cuentas.modelo.Transacciones;

public class TransaccionesMapper {

    public static Transacciones toEntity(TransaccionesSolicitudDTO dto) {
        if (dto == null) return null;
        Transacciones t = new Transacciones();
        // Origen
        if (dto.getIdCuentaClienteOrigen() != null) {
            t.setIdCuentaCliente(new CuentasClientes(dto.getIdCuentaClienteOrigen()));
        }
        // Destino (puede ser null para depósito/retiro)
        t.setIdCuentaClienteDestino(dto.getIdCuentaClienteDestino());
        t.setTipoTransaccion(dto.getTipoTransaccion());
        t.setMonto(dto.getMonto());
        t.setDescripcion(dto.getDescripcion());
        return t;
    }

    public static TransaccionesRespuestaDTO toDto(Transacciones entity) {
        if (entity == null) return null;
        return TransaccionesRespuestaDTO.builder()
            .id(entity.getId())
            .idCuentaClienteOrigen(entity.getIdCuentaCliente().getId())
            .idCuentaClienteDestino(entity.getIdCuentaClienteDestino())   // ahora compila
            .tipoTransaccion(entity.getTipoTransaccion())
            .monto(entity.getMonto())
            .descripcion(entity.getDescripcion())
            .fechaTransaccion(entity.getFechaTransaccion())
            .estado(entity.getEstado())
            .version(entity.getVersion())
            .build();
    }
}

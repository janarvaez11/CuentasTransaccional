package com.banquito.core.cuentas.mapper;

import java.math.BigDecimal;

import com.banquito.core.cuentas.dto.CuentaRespuestaDTO_Min2;
import com.banquito.core.cuentas.dto.CuentasClientesRespuestaDTO;
import com.banquito.core.cuentas.dto.CuentasClientesSolicitudDTO;
import com.banquito.core.cuentas.modelo.Cuentas;
import com.banquito.core.cuentas.modelo.CuentasClientes;

public class CuentasClientesMapper {

    public static CuentasClientes toCuentasClientes(CuentasClientesSolicitudDTO dto) {
        if (dto == null)
            return null;

        CuentasClientes entity = new CuentasClientes();
        if (dto.getIdCuenta() != null) {
            entity.setIdCuenta(new Cuentas(dto.getIdCuenta()));
        }
        entity.setIdCliente(dto.getIdCliente());

        // Se generan automáticamente después en el servicio:
        entity.setSaldoDisponible(BigDecimal.ZERO);
        entity.setSaldoContable(BigDecimal.ZERO);

        return entity;
    }

    public static CuentasClientesRespuestaDTO toCuentasClientesRespuestaDTO(CuentasClientes entity) {
        if (entity == null) {
            return null;
        }
        return CuentasClientesRespuestaDTO.builder()
                .id(entity.getId())
                .idCuenta(entity.getIdCuenta() != null ? CuentaRespuestaDTO_Min2.builder()
                        .id(entity.getIdCuenta().getId())
                        .codigoCuenta(entity.getIdCuenta().getCodigoCuenta())
                        .nombre(entity.getIdCuenta().getNombre())
                        .build() : null)
                .idCliente(entity.getIdCliente())
                .numeroCuenta(entity.getNumeroCuenta())
                .saldoDisponible(entity.getSaldoDisponible())
                .saldoContable(entity.getSaldoContable())
                .fechaApertura(entity.getFechaApertura())
                .estado(entity.getEstado())
                .version(entity.getVersion())
                .build();
    }
}
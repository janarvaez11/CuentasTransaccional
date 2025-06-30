// src/main/java/com/banquito/core/cuentas/mapper/CuentaMapper.java
package com.banquito.core.cuentas.mapper;

import com.banquito.core.cuentas.dto.CuentaRespuestaDTO;
import com.banquito.core.cuentas.dto.CuentaSolicitudDTO;
import com.banquito.core.cuentas.dto.TipoCuentaDTO;                    // <– nuevo import
import com.banquito.core.cuentas.dto.TasaInteresRespuestaDTO_IdOnly;
import com.banquito.core.cuentas.modelo.Cuentas;

public class CuentaMapper {

    public static Cuentas toEntity(CuentaSolicitudDTO dto) {
        if (dto == null) return null;
        Cuentas e = new Cuentas();
        e.setTipoCuentaId(dto.getIdTipoCuenta());
        e.setTasaInteresId(dto.getIdTasaInteres());
        e.setCodigoCuenta(dto.getCodigoCuenta());
        e.setNombre(dto.getNombre());
        e.setDescripcion(dto.getDescripcion());
        return e;
    }

    public static CuentaRespuestaDTO toDto(
        Cuentas entity,
        TipoCuentaDTO tipoCuenta,                                    // <– aquí
        TasaInteresRespuestaDTO_IdOnly tasaInteres
    ) {
        if (entity == null) return null;
        return CuentaRespuestaDTO.builder()
            .id(entity.getId())
            .idTipoCuenta(tipoCuenta)                                 // <– aquí
            .idTasaInteres(tasaInteres)
            .codigoCuenta(entity.getCodigoCuenta())
            .nombre(entity.getNombre())
            .descripcion(entity.getDescripcion())
            .fechaCreacion(entity.getFechaCreacion())
            .fechaModificacion(entity.getFechaModificacion())
            .estado(entity.getEstado().name())
            .version(entity.getVersion())
            .build();
    }
}

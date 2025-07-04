package com.banquito.core.cuentas.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class CuentaRespuestaDTO {
    private Integer id;
    private TipoCuentaDTO idTipoCuenta;
    private TasaInteresRespuestaDTO_IdOnly idTasaInteres;
    private String codigoCuenta;
    private String nombre;
    private String descripcion;
    private Instant fechaCreacion;
    private Instant fechaModificacion;
    private String estado;
    private Long version;
}

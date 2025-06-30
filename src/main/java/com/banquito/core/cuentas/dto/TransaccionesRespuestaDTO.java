package com.banquito.core.cuentas.dto;
import com.banquito.core.cuentas.enums.TipoTransaccionEnum;
import com.banquito.core.cuentas.enums.EstadoTransaccionesEnum;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder
public class TransaccionesRespuestaDTO {
    private Integer id;
    private Integer idCuentaClienteOrigen;
    private Integer idCuentaClienteDestino;
    private TipoTransaccionEnum tipoTransaccion;      // <— enum en vez de String
    private BigDecimal monto;
    private String descripcion;
    private Instant fechaTransaccion;
    private EstadoTransaccionesEnum estado;           // <— enum en vez de String
    private Long version;
}

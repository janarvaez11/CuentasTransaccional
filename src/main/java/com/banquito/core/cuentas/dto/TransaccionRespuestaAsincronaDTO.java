package com.banquito.core.cuentas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionRespuestaAsincronaDTO {
    private String mensaje;
    private String transaccionId;
    private String estado;
    private String tipoTransaccion;
}

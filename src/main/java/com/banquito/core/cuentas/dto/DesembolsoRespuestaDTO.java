package com.banquito.core.cuentas.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DesembolsoRespuestaDTO {
    private TransaccionesRespuestaDTO retiroPool;
    private TransaccionesRespuestaDTO depositoCliente;
    private TransaccionesRespuestaDTO transferenciaOrigen;
}

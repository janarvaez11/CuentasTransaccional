package com.banquito.core.cuentas.dto;

import com.banquito.core.cuentas.enums.TipoTransaccionEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransaccionesSolicitudDTO {

    @NotBlank(message = "El número de cuenta origen es obligatorio")
    @Size(min = 10, max = 10, message = "El número de cuenta debe tener exactamente 10 caracteres")
    private String numeroCuentaOrigen;

    @Size(min = 10, max = 10, message = "El número de cuenta destino debe tener exactamente 10 caracteres")
    private String numeroCuentaDestino;

    @NotNull(message = "El tipo de transacción es obligatorio")
    private TipoTransaccionEnum tipoTransaccion;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
    private BigDecimal monto;

    @Size(max = 150, message = "La descripción no puede exceder los 150 caracteres")
    private String descripcion;
}
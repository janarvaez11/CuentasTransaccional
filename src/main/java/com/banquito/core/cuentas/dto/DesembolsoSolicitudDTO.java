package com.banquito.core.cuentas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DesembolsoSolicitudDTO {

    /** ID de la cuenta del cliente (CuentasClientes.id) */
    @NotNull(message = "El ID de la cuenta del cliente es obligatorio")
    private Integer idCuentaCliente;

    /** ID de la cuenta de originación / concesionaria (CuentasClientes.id) */
    @NotNull(message = "El ID de la cuenta de originación es obligatorio")
    private Integer idCuentaOriginacion;

    /** Monto a desembolsar */
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
    private BigDecimal monto;

    /** Descripción opcional */
    @Size(max = 150, message = "La descripción no puede exceder los 150 caracteres")
    private String descripcion;
}

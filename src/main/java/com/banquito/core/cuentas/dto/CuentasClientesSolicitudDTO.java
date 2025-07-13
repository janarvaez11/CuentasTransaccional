package com.banquito.core.cuentas.dto;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Data
@Builder
public class CuentasClientesSolicitudDTO {

    @NotNull(message = "El ID de la cuenta es obligatorio")
    private Integer idCuenta;

    @NotBlank(message = "El ID del cliente es obligatorio")
    private String idCliente;
}

package com.banquito.core.cuentas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosClienteDTO {

    private String id; // ID del cliente como string (ej: "1753898188")
    private String numeroIdentificacion;
    private String tipoIdentificacion;
}
package com.banquito.core.cuentas.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {

    private String id; // ID del cliente como string (ej: "1753898188")
    private String numeroIdentificacion;
    private String tipoIdentificacion;
}

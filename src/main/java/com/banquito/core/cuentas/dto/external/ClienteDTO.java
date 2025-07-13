package com.banquito.core.cuentas.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDTO {
    private String id;
    private String idEntidad;
    private String tipoEntidad;
    private String nombre;
    private String nacionalidad;
    private String tipoIdentificacion;
    private String numeroIdentificacion;
    private String tipoCliente;
    private String segmento;
    private String canalAfiliacion;
    private String comentarios;
    private String estado;
}

/*
 @Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClienteDTO {
    private String id;
    private String numeroIdentificacion;
    private String tipoIdentificacion;
}

 */
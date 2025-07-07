package com.banquito.core.cuentas.cliente;


import com.banquito.core.cuentas.dto.DatosGeneralDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
  name = "general-service",                // nombre del servicio destino
  url  = "${general.service.url}"         // ej. http://localhost:8081
)
public interface DatosGeneral {

  @GetMapping("/api/externo/monedas/{codigoMoneda}")
  ResponseEntity<DatosGeneralDTO> findById(@PathVariable("idMoneda") String idMoneda);


}

package com.banquito.core.cuentas.cliente;

import com.banquito.core.cuentas.dto.TasaInteresRespuestaDTO_IdOnly;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
  name = "tasa-interes-cliente",
  url  = "${cuentas.configuracion.url}"
)
public interface TasaInteresCliente {
    @GetMapping("/api/cuentas/v1/tasas-intereses/{id}")
    TasaInteresRespuestaDTO_IdOnly obtenerPorId(@PathVariable("id") String id);
}

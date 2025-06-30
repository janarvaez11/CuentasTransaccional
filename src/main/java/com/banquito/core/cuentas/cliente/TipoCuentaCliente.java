package com.banquito.core.cuentas.cliente;

import com.banquito.core.cuentas.dto.TipoCuentaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
  name = "tipo-cuenta-cliente",                // nombre del servicio destino
  url  = "${cuentas.configuracion.url}"         // ej. http://localhost:8081
)
public interface TipoCuentaCliente {

  @GetMapping("/api/tipos-cuentas/{id}")
  TipoCuentaDTO obtenerPorId(@PathVariable("id") String id);

}

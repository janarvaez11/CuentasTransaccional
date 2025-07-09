package com.banquito.core.cuentas.cliente;

import com.banquito.core.cuentas.dto.external.ClienteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;

@FeignClient(name = "clientes-service", url = "${clientes.service.url}")
public interface ClientesClient {

    @GetMapping("/api/v1/clientes/clientes/{idCliente}")
    ResponseEntity<ClienteDTO> findById(@PathVariable("idCliente") String idCliente);
}

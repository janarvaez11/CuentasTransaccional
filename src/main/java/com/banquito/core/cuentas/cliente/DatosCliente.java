package com.banquito.core.cuentas.cliente;

import com.banquito.core.cuentas.dto.DatosClienteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;

@FeignClient(name = "clientes-service", url = "${clientes.service.url}")
public interface DatosCliente {

    @GetMapping("/api/v1/clientes/{idCliente}")
    ResponseEntity<DatosClienteDTO> findById(
        @PathVariable("idCliente") String idCliente
    );
}

package com.banquito.core.cuentas.cliente;

import com.banquito.core.cuentas.dto.external.ClienteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "clientes-service", url = "${clientes.service.url}")
public interface ClientesClient {

    @GetMapping("/api/v1/clientes/clientes")
    ResponseEntity<List<ClienteDTO>> findByTipoYNumeroIdentificacion(
        @RequestParam("tipoIdentificacion") String tipoIdentificacion,
        @RequestParam("numeroIdentificacion") String numeroIdentificacion
    );
}

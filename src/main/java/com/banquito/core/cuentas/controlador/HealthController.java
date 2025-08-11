package com.banquito.core.cuentas.controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> rootHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "cuentas");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/api/cuentas/health")
    public ResponseEntity<Map<String, Object>> cuentasHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "cuentas");
        return ResponseEntity.ok(status);
    }
}



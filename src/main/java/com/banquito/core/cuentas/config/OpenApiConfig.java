package com.banquito.core.cuentas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Cuentas")
                        .version("v1")
                        .description("Documentación de los endpoints de cuentas"))
                .servers(List.of(
                        new Server()
                                .url("http://banquito-alb-1166574131.us-east-2.elb.amazonaws.com/api/cuentas")
                                .description("Servidor AWS (Producción)"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor Local (Desarrollo)")
                ));
    }
}
package br.edu.ifsp.consulta.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Consulta Service API")
                .version("1.0")
                .description("Microsserviço para gerenciamento de consultas médicas")
                .contact(new Contact()
                    .name("Equipe Consulta Fácil")
                    .email("contato@consulta-facil.com")))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Token JWT obtido através do monolito (consulta-facil-api)")))
            .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
} 
package com.lafl.quote;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "LAFL Quote Service API",
        version = "v1",
        description = "Quote intake, contact intake, and ops overview endpoints."
    )
)
public class QuoteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuoteServiceApplication.class, args);
    }

    @Bean
    SecurityScheme bearerSecurityScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");
    }

    @Bean
    OpenAPI quoteOpenApi(SecurityScheme bearerSecurityScheme) {
        return new OpenAPI()
            .components(new Components().addSecuritySchemes("bearer", bearerSecurityScheme));
    }
}

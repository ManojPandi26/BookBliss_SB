package com.BookBliss.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String[] PUBLIC_URLS = {
            // Auth endpoints
            "/api/v1/auth/**",

            // Book endpoints - General access
            "/api/v1/books/**",
            "/api/v1/books/by-author/**",
            "/api/v1/books/{id}/similar",

            // Reviews Endpoints
            "/api/v1/reviews/book/**",
            "/api/v1/reviews/books/**",

            // General Errors
            "/error"
    };

    @Value("${spring.application.name:BookBliss API}")
    private String applicationName;

    @Value("${api.version:1.0}")
    private String apiVersion;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " Documentation")
                        .description("API documentation for BookBliss authentication and book management services")
                        .version(apiVersion)
                        .contact(new Contact()
                                .name("BookBliss Support")
                                .email("support@BookBliss.com")
                                .url("https://www.BookBliss.com/support"))
                        .license(new License()
                                .name("BookBliss License")
                                .url("https://www.BookBliss.com/license")))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Production Server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
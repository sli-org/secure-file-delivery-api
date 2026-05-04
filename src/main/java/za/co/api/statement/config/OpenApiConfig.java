package za.co.api.statement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI documentation configuration for statement-api.
 * 
 * This configuration class sets up comprehensive API documentation using OpenAPI 3.0 specification.
 * Provides detailed API information including contact details, licensing, and JWT authentication.
 */
@Configuration
public class OpenApiConfig {

    private final BuildProperties buildProperties;

    public OpenApiConfig(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public OpenAPI statementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Statement Management API")
                        .description("""
                                RESTful API for managing statements.
                                
                                ## Authentication
                                This API requires JWT Bearer tokens obtained from the auth API.
                                
                                All requests must include a valid JWT token in the Authorization header:
                                ```
                                Authorization: Bearer <your-jwt-token>
                                ```
                                
                                ## Authorization
                                Access to endpoints is controlled by roles and scopes assigned to your token.
                                Your token must have appropriate permissions for the operations you wish to perform.
                                
                                ### Operations
                                - **Read**: View statements
                                - **Create**: Upload new statements
                                - **Delete**: Remove statements
                                - **Download**: Generate and use secure download links
                                
                                ## Public Endpoints
                                - `GET /api/v1/statements/download/{token}` - Token-based download (no JWT required)
                                
                                ## Error Responses
                                - `401 Unauthorized` - Invalid or missing JWT token
                                - `403 Forbidden` - Valid token but insufficient permissions
                                - `404 Not Found` - Resource doesn't exist
                                - `410 Gone` - Token expired or download limit reached
                                - `422 Unprocessable Entity` - Validation errors
                                
                                For authentication and authorization issues, contact your system administrator.
                                """)
                        .version(buildProperties.getVersion())
                        .contact(new Contact()
                                .name("Development Team")
                                .email("dev@local")
                                .url("https://example.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://example.com/license")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer token obtained from auth API. Paste your access token here.")));
    }

    /**
     * Configure grouped OpenAPI to exclude OPTIONS methods (CORS preflight).
     * This prevents CORS preflight OPTIONS endpoints from appearing in Swagger documentation.
     *
     * @return the grouped OpenAPI configuration for public endpoints
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/v1/**")
                .addOpenApiCustomizer(openApiCustomizer())
                .build();
    }

    /**
     * Customizer to remove OPTIONS methods from OpenAPI documentation.
     *
     * @return the OpenAPI customizer that removes OPTIONS methods
     */
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) -> {
            pathItem.setOptions(null);
        });
    }
}

package za.co.api.statement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Jackson configuration for handling Java 8 types like Optional.
 * 
 * <p>Required for DTOs that use Optional&lt;String&gt; for optional fields.
 * With Spring Boot 4 / Jackson 3.x, modules are auto-discovered.</p>
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }
}

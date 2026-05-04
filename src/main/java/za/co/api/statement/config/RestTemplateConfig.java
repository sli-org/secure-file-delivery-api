package za.co.api.statement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration class for REST client components.
 * 
 * <p>This configuration provides beans for external API communication,
 * including RestTemplate for HTTP operations with proper timeout
 * configuration from application properties.</p>
 * 
 * <p>Used for Azure Blob Storage API integration.</p>
 */
@Configuration
public class RestTemplateConfig {

    @Value("${external.api.timeout.connect:5000}")
    private int connectTimeout;

    @Value("${external.api.timeout.read:30000}")
    private int readTimeout;

    /**
     * Creates a RestTemplate bean for making HTTP requests to external APIs.
     * Configures timeouts based on external.api.timeout properties.
     * 
     * @return RestTemplate instance configured with timeouts from properties
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        factory.setReadTimeout(Duration.ofMillis(readTimeout));
        return new RestTemplate(factory);
    }
}

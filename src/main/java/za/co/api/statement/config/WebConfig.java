package za.co.api.statement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Web configuration for the Statement API.
 * 
 * <p>Enables asynchronous processing for event publishing and other async operations.</p>
 */
@Configuration
@EnableAsync
public class WebConfig {
    // Async configuration enabled for event processing
}

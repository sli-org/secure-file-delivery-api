package za.co.api.statement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Statement API.
 * 
 * <p>This application provides RESTful APIs for managing statements, including:</p>
 * <ul>
 *   <li>CRUD operations for statement entity management</li>
 *   <li>Event-driven architecture with RabbitMQ integration</li>
 *   <li>Comprehensive validation and error handling</li>
 *   <li>OpenAPI documentation with Swagger UI</li>
 * </ul>
 * 
 * @author Development Team
 * @version 1.0.0-SNAPSHOT
 * @since 2025-09-25
 */
@Slf4j
@SpringBootApplication
public class Application {

	/**
	 * Main entry point for the Spring Boot application.
	 * 
	 * @param args command line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		log.info("Statement API has started successfully.");
	}
}

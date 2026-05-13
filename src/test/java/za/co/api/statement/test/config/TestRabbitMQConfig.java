package za.co.api.statement.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestRabbitMQConfig {

    @Bean
    @Primary
    public String eventsExchangeName() {
        return "test-exchange";
    }

    @Bean
    @Primary
    public String eventsQueueName() {
        return "test-queue";
    }

    @Bean
    @Primary
    public String eventsRoutingKey() {
        return "test.routing.key";
    }
}
package za.co.api.statement.service;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import za.co.api.statement.dto.StatementDTO;
import za.co.api.statement.dto.StatementEventDTO;
import za.co.api.statement.dto.code.StatementEventTypeCode;

/**
 * Service class for Statement Events.
 * Publishes events to RabbitMQ for async processing.
 */
@Slf4j
@Service
public class StatementEventService {

    @Value("${events.exchange-name}")
    private String exchangeName;

    @Value("${events.routing-key.statement-uploaded}")
    private String uploadedRoutingKey;

    @Value("${events.routing-key.download-link-created}")
    private String downloadLinkCreatedRoutingKey;

    @Value("${events.routing-key.statement-deleted}")
    private String deletedRoutingKey;

    private final RabbitTemplate rabbitTemplate;
    
    public StatementEventService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Async
    public void publishStatementUploadedEvent(StatementDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Statement DTO cannot be null");
        }
        validateRoutingConfig(uploadedRoutingKey, "Uploaded routing key");

        StatementEventDTO eventDTO = buildEvent(
                dto.getId() != null ? dto.getId() : UUID.randomUUID().toString(),
                StatementEventTypeCode.CREATED, dto);

        publishEvent(uploadedRoutingKey, eventDTO, "Statement Uploaded");
    }

    @Async
    public void publishDownloadLinkCreatedEvent(StatementDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Statement DTO cannot be null");
        }
        validateRoutingConfig(downloadLinkCreatedRoutingKey, "Download link created routing key");

        StatementEventDTO eventDTO = buildEvent(
                dto.getId() != null ? dto.getId() : UUID.randomUUID().toString(),
                StatementEventTypeCode.UPDATED, dto);

        publishEvent(downloadLinkCreatedRoutingKey, eventDTO, "Download Link Created");
    }

    @Async
    public void publishStatementDeletedEvent(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Statement ID cannot be null or empty");
        }
        validateRoutingConfig(deletedRoutingKey, "Deleted routing key");

        StatementEventDTO eventDTO = buildEvent(id, StatementEventTypeCode.DELETED, null);

        publishEvent(deletedRoutingKey, eventDTO, "Statement Deleted");
    }

    private void validateRoutingConfig(String routingKey, String routingKeyName) {
        if (exchangeName == null || exchangeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Exchange name cannot be null or empty");
        }
        if (routingKey == null || routingKey.trim().isEmpty()) {
            throw new IllegalArgumentException(routingKeyName + " cannot be null or empty");
        }
    }

    private StatementEventDTO buildEvent(String id, StatementEventTypeCode eventType, StatementDTO data) {
        StatementEventDTO eventDTO = new StatementEventDTO();
        eventDTO.setId(id);
        eventDTO.setEventType(eventType);
        eventDTO.setTimestamp(java.time.Instant.now());
        eventDTO.setData(data);
        return eventDTO;
    }

    private void publishEvent(String routingKey, StatementEventDTO eventDTO, String eventName) {
        log.debug("Using Exchange Name: {}", exchangeName);
        log.debug("Using Routing Key: {}", routingKey);

        rabbitTemplate.convertAndSend(exchangeName, routingKey, eventDTO);
        log.info("Published {} Event with routing key: {} and ID: {}",
                eventName, routingKey, eventDTO.getId());
    }
}

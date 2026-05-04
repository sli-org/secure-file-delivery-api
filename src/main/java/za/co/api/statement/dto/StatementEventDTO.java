package za.co.api.statement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.api.statement.dto.code.StatementEventTypeCode;

/**
 * Wrapper DTO for Statement RabbitMQ event messages.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Wrapper for Statement event messages")
public class StatementEventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Event identifier")
    private String id;

    @Schema(description = "Event type")
    private StatementEventTypeCode eventType;

    @Schema(description = "Event timestamp")
    private Instant timestamp;

    @Schema(description = "Event payload data")
    private StatementDTO data;
}

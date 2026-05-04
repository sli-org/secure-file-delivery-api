package za.co.api.statement.dto.code;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import za.co.common.base.BaseEnum;

/**
 * Enum for Statement Event Types.
 * Values: CREATED, UPDATED, DELETED (no entity prefix on values).
 */
@Schema(description = "Statement Event Type")
public enum StatementEventTypeCode implements BaseEnum<StatementEventTypeCode> {

    @Schema(description = "Statement created event")
    CREATED("CREATED", "Statement created event"),

    @Schema(description = "Statement updated event")
    UPDATED("UPDATED", "Statement updated event"),

    @Schema(description = "Statement deleted event")
    DELETED("DELETED", "Statement deleted event");

    private final String value;
    private final String description;

    StatementEventTypeCode(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @JsonValue
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static StatementEventTypeCode fromValue(String value) {
        return BaseEnum.fromValue(value, StatementEventTypeCode.class);
    }
}

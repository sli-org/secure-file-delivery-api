package za.co.api.statement.dto.code;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import za.co.common.base.BaseEnum;

/**
 * Enum for Statement Status.
 * Maps internal values to external API codes.
 */
@Schema(description = "Statement Status")
public enum StatementStatusCode implements BaseEnum<StatementStatusCode> {

    @Schema(description = "Statement uploaded and available for download")
    AVAILABLE("AVAILABLE", "Statement uploaded and available for download", "AVAIL"),

    @Schema(description = "Statement past retention period")
    EXPIRED("EXPIRED", "Statement past retention period", "EXP"),

    @Schema(description = "Soft-deleted")
    DELETED("DELETED", "Soft-deleted", "DEL");

    private final String value;
    private final String description;
    private final String externalCode;

    StatementStatusCode(String value, String description, String externalCode) {
        this.value = value;
        this.description = description;
        this.externalCode = externalCode;
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

    public String getExternalCode() {
        return externalCode;
    }

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static StatementStatusCode fromValue(String value) {
        return BaseEnum.fromValue(value, StatementStatusCode.class);
    }

    public static StatementStatusCode fromExternalCode(String externalCode) {
        if (externalCode == null) {
            return null;
        }
        for (StatementStatusCode status : values()) {
            if (status.externalCode.equals(externalCode)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No StatementStatusCode found for external code: " + externalCode);
    }
}

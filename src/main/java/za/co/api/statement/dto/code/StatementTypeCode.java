package za.co.api.statement.dto.code;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import za.co.common.base.BaseEnum;

/**
 * Enum for Statement Type classification.
 * Maps internal values to external API codes.
 */
@Schema(description = "Statement Type classification")
public enum StatementTypeCode implements BaseEnum<StatementTypeCode> {

    @Schema(description = "Monthly account statement")
    MONTHLY("MONTHLY", "Monthly account statement", "MTH"),

    @Schema(description = "Annual summary statement")
    ANNUAL("ANNUAL", "Annual summary statement", "ANN"),

    @Schema(description = "Tax certificate")
    TAX("TAX", "Tax certificate", "TAX"),

    @Schema(description = "On-demand statement")
    AD_HOC("AD_HOC", "On-demand statement", "ADH");

    private final String value;
    private final String description;
    private final String externalCode;

    StatementTypeCode(String value, String description, String externalCode) {
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

    /**
     * Converts from internal value (for JSON deserialization).
     *
     * @param value the internal value to convert
     * @return the corresponding enum value
     * @throws IllegalArgumentException if no matching value is found
     */
    @JsonCreator
    public static StatementTypeCode fromValue(String value) {
        return BaseEnum.fromValue(value, StatementTypeCode.class);
    }

    /**
     * Converts from external API code.
     *
     * @param externalCode the external system code
     * @return the corresponding enum value, or null if input is null
     * @throws IllegalArgumentException if no matching code is found for non-null input
     */
    public static StatementTypeCode fromExternalCode(String externalCode) {
        if (externalCode == null) {
            return null;
        }
        for (StatementTypeCode type : values()) {
            if (type.externalCode.equals(externalCode)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No StatementTypeCode found for external code: " + externalCode);
    }
}

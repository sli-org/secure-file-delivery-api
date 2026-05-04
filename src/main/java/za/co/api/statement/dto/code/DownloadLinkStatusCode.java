package za.co.api.statement.dto.code;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import za.co.common.base.BaseEnum;

/**
 * Enum for DownloadLink Status.
 * Maps internal values to external API codes.
 */
@Schema(description = "Download Link Status")
public enum DownloadLinkStatusCode implements BaseEnum<DownloadLinkStatusCode> {

    @Schema(description = "Link is valid and usable")
    ACTIVE("ACTIVE", "Link is valid and usable", "ACT"),

    @Schema(description = "Maximum downloads reached")
    USED("USED", "Maximum downloads reached", "USED"),

    @Schema(description = "Link has expired")
    EXPIRED("EXPIRED", "Link has expired", "EXP"),

    @Schema(description = "Link manually revoked")
    REVOKED("REVOKED", "Link manually revoked", "REV");

    private final String value;
    private final String description;
    private final String externalCode;

    DownloadLinkStatusCode(String value, String description, String externalCode) {
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
    public static DownloadLinkStatusCode fromValue(String value) {
        return BaseEnum.fromValue(value, DownloadLinkStatusCode.class);
    }

    public static DownloadLinkStatusCode fromExternalCode(String externalCode) {
        if (externalCode == null) {
            return null;
        }
        for (DownloadLinkStatusCode status : values()) {
            if (status.externalCode.equals(externalCode)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No DownloadLinkStatusCode found for external code: " + externalCode);
    }
}

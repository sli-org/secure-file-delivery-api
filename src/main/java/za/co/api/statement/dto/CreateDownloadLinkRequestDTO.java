package za.co.api.statement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * Request DTO for generating a secure download link.
 */
@Data
@Schema(description = "Request to generate a secure download link for a statement")
public class CreateDownloadLinkRequestDTO {

    private static final int DEFAULT_EXPIRES_IN_HOURS = 24;
    private static final int DEFAULT_MAX_DOWNLOADS = 1;

    @Schema(description = "Hours until the link expires (default 24)",
            example = "24",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable
    @Min(value = 1, message = "Expires in hours must be at least 1")
    @Max(value = 720, message = "Expires in hours cannot exceed 720")
    private Integer expiresInHours;

    @Schema(description = "Maximum allowed downloads (default 1)",
            example = "1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable
    @Min(value = 1, message = "Max downloads must be at least 1")
    @Max(value = 10, message = "Max downloads cannot exceed 10")
    private Integer maxDownloads;

    /**
     * Returns expiresInHours or default value.
     *
     * @return the configured expiration hours, or the default if not set
     */
    public int getEffectiveExpiresInHours() {
        return expiresInHours != null ? expiresInHours : DEFAULT_EXPIRES_IN_HOURS;
    }

    /**
     * Returns maxDownloads or default value.
     *
     * @return the configured max downloads, or the default if not set
     */
    public int getEffectiveMaxDownloads() {
        return maxDownloads != null ? maxDownloads : DEFAULT_MAX_DOWNLOADS;
    }
}

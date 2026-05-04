package za.co.api.statement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;
import za.co.api.statement.dto.code.DownloadLinkStatusCode;
import za.co.common.base.BaseDTO;

/**
 * Data Transfer Object for DownloadLink.
 * Represents a secure, time-limited download link for a statement.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Download link entity representing a secure, time-limited download link")
public class DownloadLinkDTO extends BaseDTO {

    /** Validation group for create operations (POST). */
    public interface OnCreate {}

    @Schema(description = "Unique identifier (system-assigned UUID)",
            example = "660e8400-e29b-41d4-a716-446655440001",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Size(max = 36, message = "ID cannot exceed 36 characters")
    @Nullable
    private String id;

    @Schema(description = "Reference to parent Statement",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(groups = {OnCreate.class}, message = "Statement ID is required")
    @Size(max = 36, groups = {Default.class, OnCreate.class},
          message = "Statement ID cannot exceed 36 characters")
    private String statementId;

    @Schema(description = "HMAC-signed download token",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    @Size(max = 500, message = "Token cannot exceed 500 characters")
    private String token;

    @Schema(description = "Full download URL including token",
            example = "https://api.example.com/api/v1/statements/download/eyJzdGF0ZW1lbnRJZCI6...",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    private String downloadUrl;

    @Schema(description = "Token expiration timestamp",
            example = "2024-01-16T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    private LocalDateTime expiresAt;

    @Schema(description = "Maximum allowed downloads (default 1)",
            example = "1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable
    @Min(value = 1, groups = {Default.class, OnCreate.class},
         message = "Max downloads must be at least 1")
    @Max(value = 10, groups = {Default.class, OnCreate.class},
         message = "Max downloads cannot exceed 10")
    private Integer maxDownloads;

    @Schema(description = "Number of times downloaded",
            example = "0",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    private Integer downloadCount;

    @Schema(description = "Link status",
            example = "ACTIVE",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    private DownloadLinkStatusCode status;

    @Schema(description = "Last download timestamp",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    private LocalDateTime downloadedAt;

    @Schema(description = "IP address of last downloader",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    @Size(max = 45, message = "IP address cannot exceed 45 characters")
    private String downloadedByIp;

    @Schema(description = "Record creation timestamp",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    private LocalDateTime createdAt;

    @Schema(description = "User who generated the link",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    private String createdBy;
}

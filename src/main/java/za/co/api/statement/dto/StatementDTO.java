package za.co.api.statement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;
import za.co.api.statement.dto.code.StatementStatusCode;
import za.co.api.statement.dto.code.StatementTypeCode;
import za.co.common.base.BaseDTO;

/**
 * Data Transfer Object for Statement.
 * Uses validation groups: OnCreate (POST), Default (responses).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Statement entity representing a customer account statement")
public class StatementDTO extends BaseDTO {

    /** Validation group for create operations (POST). */
    public interface OnCreate {}

    /** Validation group for update operations (PUT). */
    public interface OnUpdate {}

    @Schema(description = "Unique identifier (system-assigned UUID)",
            example = "550e8400-e29b-41d4-a716-446655440000",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Size(max = 36, message = "ID cannot exceed 36 characters")
    @Nullable
    private String id;

    @Schema(description = "Customer identifier",
            example = "CUST-12345",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(groups = {OnCreate.class}, message = "Customer ID is required")
    @Size(max = 50, groups = {Default.class, OnCreate.class},
          message = "Customer ID cannot exceed 50 characters")
    private String customerId;

    @Schema(description = "Statement period end date",
            example = "2024-01-31T00:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(groups = {OnCreate.class}, message = "Statement date is required")
    private LocalDateTime statementDate;

    @Schema(description = "Type of statement",
            example = "MONTHLY",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(groups = {OnCreate.class}, message = "Statement type is required")
    private StatementTypeCode statementType;

    @Schema(description = "Account number (masked in responses, e.g. ****1234)",
            example = "****1234",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(groups = {OnCreate.class}, message = "Account number is required")
    @Size(max = 20, groups = {Default.class, OnCreate.class},
          message = "Account number cannot exceed 20 characters")
    private String accountNumber;

    @Schema(description = "Original PDF filename",
            example = "statement-2024-01.pdf",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(groups = {OnCreate.class}, message = "File name is required")
    @Size(max = 255, groups = {Default.class, OnCreate.class},
          message = "File name cannot exceed 255 characters")
    private String fileName;

    @Schema(description = "File size in bytes",
            example = "125432",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    private Long fileSize;

    @Schema(description = "SHA-256 hash of file content for integrity verification",
            example = "a3f2b8c1d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    @Size(max = 64, message = "Content hash cannot exceed 64 characters")
    private String contentHash;

    @Schema(description = "Storage path in blob container",
            example = "statements/CUST-12345/550e8400.pdf",
            accessMode = Schema.AccessMode.READ_ONLY,
            hidden = true)
    @JsonIgnore
    @Nullable
    @Size(max = 500, message = "Blob path cannot exceed 500 characters")
    private String blobPath;

    @Schema(description = "Statement status",
            example = "AVAILABLE",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    private StatementStatusCode status;

    @Schema(description = "Days to retain before expiry (default 365)",
            example = "365",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable
    @Min(value = 30, groups = {Default.class, OnCreate.class},
         message = "Retention days must be at least 30")
    @Max(value = 2555, groups = {Default.class, OnCreate.class},
         message = "Retention days cannot exceed 2555")
    private Integer retentionDays;

    @Schema(description = "Record creation timestamp",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    private LocalDateTime createdAt;

    @Schema(description = "Record last update timestamp",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    private LocalDateTime updatedAt;

    @Schema(description = "User who created record",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    private String createdBy;

    @Schema(description = "User who last updated record",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    private String updatedBy;
}

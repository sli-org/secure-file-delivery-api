package za.co.api.statement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.api.statement.dto.code.StatementStatusCode;
import za.co.api.statement.dto.code.StatementTypeCode;

/**
 * JPA Entity for Statement.
 * Represents a customer account statement with metadata and storage reference.
 */
@Entity
@Table(name = "STATEMENT")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_RETENTION_DAYS = 365;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false, length = 36)
    private String id;

    @Column(name = "CUSTOMER_ID", nullable = false, length = 50)
    private String customerId;

    @Column(name = "STATEMENT_DATE", nullable = false)
    private LocalDateTime statementDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATEMENT_TYPE", nullable = false, length = 20)
    private StatementTypeCode statementType;

    @Column(name = "ACCOUNT_NUMBER", nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "FILE_NAME", nullable = false, length = 255)
    private String fileName;

    @Column(name = "FILE_SIZE")
    private Long fileSize;

    @Column(name = "CONTENT_HASH", length = 64)
    private String contentHash;

    @Column(name = "BLOB_PATH", length = 500)
    private String blobPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20)
    private StatementStatusCode status;

    @Column(name = "RETENTION_DAYS")
    private Integer retentionDays;

    @Version
    @Column(name = "VERSION")
    private Long version;

    // Audit fields - Spring Data JPA Auditing (greenfield API)
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "CREATED_BY", updatable = false, length = 255)
    private String createdBy;

    @Column(name = "UPDATED_BY", length = 255)
    private String updatedBy;
}

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

@Entity
@Table(name = "statement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;

    @Column(name = "statement_date", nullable = false,columnDefinition = "TIMESTAMP")
    private LocalDateTime statementDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "statement_type", nullable = false, length = 20)
    private StatementTypeCode statementType;

    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

    @Column(name = "blob_path", length = 500)
    private String blobPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StatementStatusCode status;

    @Column(name = "retention_days")
    private Integer retentionDays;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", updatable = false, length = 255)
    private String createdBy;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;
}
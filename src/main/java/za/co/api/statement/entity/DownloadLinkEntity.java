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
import za.co.api.statement.dto.code.DownloadLinkStatusCode;

/**
 * JPA Entity for DownloadLink.
 * Represents a secure, time-limited download link for a statement.
 */
@Entity
@Table(name = "DOWNLOAD_LINK")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownloadLinkEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false, length = 36)
    private String id;

    @Column(name = "STATEMENT_ID", nullable = false, length = 36)
    private String statementId;

    @Column(name = "TOKEN", length = 500)
    private String token;

    @Column(name = "EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "MAX_DOWNLOADS")
    private Integer maxDownloads;

    @Column(name = "DOWNLOAD_COUNT")
    private Integer downloadCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20)
    private DownloadLinkStatusCode status;

    @Column(name = "DOWNLOADED_AT")
    private LocalDateTime downloadedAt;

    @Column(name = "DOWNLOADED_BY_IP", length = 45)
    private String downloadedByIp;

    @Version
    @Column(name = "VERSION")
    private Long version;

    // Audit fields
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "CREATED_BY", updatable = false, length = 255)
    private String createdBy;
}

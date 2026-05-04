package za.co.api.statement.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.api.statement.dto.code.DownloadLinkStatusCode;
import za.co.api.statement.entity.DownloadLinkEntity;

/**
 * Repository for DownloadLink entity persistence operations.
 */
@Repository
public interface DownloadLinkRepository extends JpaRepository<DownloadLinkEntity, String> {

    /**
     * Find a download link by its token.
     *
     * @param token the HMAC-signed download token
     * @return the download link if found
     */
    Optional<DownloadLinkEntity> findByToken(String token);

    /**
     * Find all active download links for a statement.
     *
     * @param statementId the statement ID
     * @param status the link status
     * @return list of matching download links
     */
    List<DownloadLinkEntity> findByStatementIdAndStatus(String statementId, DownloadLinkStatusCode status);

    /**
     * Find all download links for a statement.
     *
     * @param statementId the statement ID
     * @return list of download links
     */
    List<DownloadLinkEntity> findByStatementId(String statementId);
}

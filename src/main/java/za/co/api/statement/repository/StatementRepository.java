package za.co.api.statement.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.api.statement.dto.code.StatementStatusCode;
import za.co.api.statement.dto.code.StatementTypeCode;
import za.co.api.statement.entity.StatementEntity;

/**
 * Repository for Statement entity persistence operations.
 */
@Repository
public interface StatementRepository extends JpaRepository<StatementEntity, String> {

    /**
     * Find statements by customer ID with optional filters, excluding DELETED status.
     *
     * @param customerId the customer identifier
     * @param statementType optional statement type filter
     * @param status optional status filter
     * @param fromDate optional from date filter (inclusive)
     * @param toDate optional to date filter (inclusive)
     * @param pageable pagination parameters
     * @return page of matching statements
     */
@Query("SELECT s FROM StatementEntity s WHERE s.customerId = :customerId " +
       "AND s.status <> 'DELETED' " +
       "AND (:statementType IS NULL OR s.statementType = :statementType) " +
       "AND (:status IS NULL OR s.status = :status) " +
       "AND (:fromDate IS NULL OR s.statementDate >= :fromDate) " +
       "AND (:toDate IS NULL OR s.statementDate <= :toDate) " +
       "ORDER BY s.statementDate DESC")
Page<StatementEntity> findByFilters(
        @Param("customerId") String customerId,
        @Param("statementType") StatementTypeCode statementType,
        @Param("status") StatementStatusCode status,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        Pageable pageable);   

    /**
     * Find statements past retention period that are still AVAILABLE.
     *
     * @param status the status to filter by
     * @param cutoffDate date before which statements are expired
     * @return list of expired statements
     */
    List<StatementEntity> findByStatusAndCreatedAtBefore(StatementStatusCode status, LocalDateTime cutoffDate);

    /**
     * Check if a non-deleted statement with the same content hash exists for a customer.
     *
     * @param contentHash SHA-256 hash of the file
     * @param customerId the customer identifier
     * @param status the status to exclude (DELETED)
     * @return true if a duplicate exists
     */
    boolean existsByContentHashAndCustomerIdAndStatusNot(String contentHash, String customerId,
            StatementStatusCode status);

    /*
    * Find statements by customer ID, excluding DELETED status, ordered by statement date descending.
    * This is used for the basic listing endpoint without filters.
    * Note: This method is defined with a native query for performance optimization, as it is a common access pattern.
     * The count query is also provided for pagination support.
     * @param customerId the customer identifier
     * @param pageable pagination parameters
     * @return page of matching statements
     *
     */
    @Query(value = "SELECT * FROM statement WHERE customer_id = :customerId AND status != 'DELETED' ORDER BY statement_date DESC",
       countQuery = "SELECT COUNT(*) FROM statement WHERE customer_id = :customerId AND status != 'DELETED'",
       nativeQuery = true)
    Page<StatementEntity> findByCustomerId(@Param("customerId") String customerId, Pageable pageable);
}

package za.co.api.statement.transformer;

import org.springframework.stereotype.Component;
import za.co.api.statement.dto.StatementDTO;
import za.co.api.statement.entity.StatementEntity;

/**
 * Transformer for Statement DTO ↔ Entity conversions.
 * Null-safe transformations. Never updates audit fields.
 */
@Component
public class StatementTransformer {

    private static final int ACCOUNT_MASK_VISIBLE_DIGITS = 4;
    private static final String ACCOUNT_MASK_PREFIX = "****";

    /**
     * Converts entity to DTO.
     * Account number is masked (BR4: show only last 4 digits).
     *
     * @param entity the entity to convert (may be null)
     * @return the converted DTO, or null if entity is null
     */
    public StatementDTO toDTO(StatementEntity entity) {
        if (entity == null) {
            return null;
        }

        StatementDTO dto = new StatementDTO();
        dto.setId(entity.getId());
        dto.setCustomerId(entity.getCustomerId());
        dto.setStatementDate(entity.getStatementDate());
        dto.setStatementType(entity.getStatementType());
        dto.setAccountNumber(maskAccountNumber(entity.getAccountNumber()));
        dto.setFileName(entity.getFileName());
        dto.setFileSize(entity.getFileSize());
        dto.setContentHash(entity.getContentHash());
        dto.setBlobPath(entity.getBlobPath());
        dto.setStatus(entity.getStatus());
        dto.setRetentionDays(entity.getRetentionDays());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    /**
     * Converts DTO to entity for creation.
     *
     * @param dto the DTO to convert (may be null)
     * @return the converted entity, or null if dto is null
     */
    public StatementEntity toEntity(StatementDTO dto) {
        if (dto == null) {
            return null;
        }

        StatementEntity entity = new StatementEntity();
        entity.setCustomerId(dto.getCustomerId());
        entity.setStatementDate(dto.getStatementDate());
        entity.setStatementType(dto.getStatementType());
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setFileName(dto.getFileName());
        entity.setRetentionDays(dto.getRetentionDays());
        return entity;
    }

    /**
     * Masks account number showing only last 4 digits (BR4).
     *
     * @param accountNumber the full account number
     * @return masked account number (e.g. "****1234"), or null if input is null
     */
    public String maskAccountNumber(String accountNumber) {
        if (accountNumber == null) {
            return null;
        }
        if (accountNumber.length() <= ACCOUNT_MASK_VISIBLE_DIGITS) {
            return accountNumber;
        }
        return ACCOUNT_MASK_PREFIX + accountNumber.substring(accountNumber.length() - ACCOUNT_MASK_VISIBLE_DIGITS);
    }
}

package za.co.api.statement.transformer;

import org.springframework.stereotype.Component;
import za.co.api.statement.dto.DownloadLinkDTO;
import za.co.api.statement.entity.DownloadLinkEntity;

/**
 * Transformer for DownloadLink DTO ↔ Entity conversions.
 * Null-safe transformations. Never updates audit fields.
 */
@Component
public class DownloadLinkTransformer {

    /**
     * Converts entity to DTO.
     *
     * @param entity the entity to convert (may be null)
     * @return the converted DTO, or null if entity is null
     */
    public DownloadLinkDTO toDTO(DownloadLinkEntity entity) {
        if (entity == null) {
            return null;
        }

        DownloadLinkDTO dto = new DownloadLinkDTO();
        dto.setId(entity.getId());
        dto.setStatementId(entity.getStatementId());
        dto.setToken(entity.getToken());
        dto.setExpiresAt(entity.getExpiresAt());
        dto.setMaxDownloads(entity.getMaxDownloads());
        dto.setDownloadCount(entity.getDownloadCount());
        dto.setStatus(entity.getStatus());
        dto.setDownloadedAt(entity.getDownloadedAt());
        dto.setDownloadedByIp(entity.getDownloadedByIp());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        return dto;
    }

    /**
     * Converts DTO to entity for creation.
     *
     * @param dto the DTO to convert (may be null)
     * @return the converted entity, or null if dto is null
     */
    public DownloadLinkEntity toEntity(DownloadLinkDTO dto) {
        if (dto == null) {
            return null;
        }

        DownloadLinkEntity entity = new DownloadLinkEntity();
        entity.setStatementId(dto.getStatementId());
        entity.setToken(dto.getToken());
        entity.setExpiresAt(dto.getExpiresAt());
        entity.setMaxDownloads(dto.getMaxDownloads());
        entity.setDownloadCount(dto.getDownloadCount());
        entity.setStatus(dto.getStatus());
        return entity;
    }
}

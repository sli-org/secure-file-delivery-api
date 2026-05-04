package za.co.api.statement.test.data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import za.co.api.statement.dto.CreateDownloadLinkRequestDTO;
import za.co.api.statement.dto.DownloadLinkDTO;
import za.co.api.statement.dto.StatementDTO;
import za.co.api.statement.dto.code.DownloadLinkStatusCode;
import za.co.api.statement.dto.code.StatementStatusCode;
import za.co.api.statement.dto.code.StatementTypeCode;
import za.co.api.statement.entity.DownloadLinkEntity;
import za.co.api.statement.entity.StatementEntity;

/**
 * Test fixtures for Statement and DownloadLink test data.
 */
public final class StatementTestFixtures {

    public static final String VALID_ID = "550e8400-e29b-41d4-a716-446655440000";
    public static final String ANOTHER_VALID_ID = "550e8400-e29b-41d4-a716-446655440001";
    public static final String NON_EXISTENT_ID = "00000000-0000-0000-0000-000000000000";
    public static final String CUSTOMER_ID = "CUST-001";
    public static final String ACCOUNT_NUMBER = "1234567890";
    public static final String MASKED_ACCOUNT_NUMBER = "******7890";
    public static final String FILE_NAME = "statement-2024-01.pdf";
    public static final String BLOB_PATH = "statements/CUST-001/2024/01/statement-2024-01.pdf";
    public static final String FILE_HASH = "abc123def456";
    public static final long FILE_SIZE = 102400L;
    public static final StatementTypeCode DEFAULT_TYPE = StatementTypeCode.MONTHLY;
    public static final StatementStatusCode DEFAULT_STATUS = StatementStatusCode.AVAILABLE;

    private StatementTestFixtures() {
    }

    // =====================================================
    // STATEMENT DTO FIXTURES
    // =====================================================

    public static StatementDTO validStatementDto() {
        StatementDTO dto = new StatementDTO();
        dto.setId(VALID_ID);
        dto.setCustomerId(CUSTOMER_ID);
        dto.setAccountNumber(MASKED_ACCOUNT_NUMBER);
        dto.setStatementDate(LocalDateTime.of(2024, 1, 31, 0, 0));
        dto.setStatementType(DEFAULT_TYPE);
        dto.setStatus(DEFAULT_STATUS);
        dto.setFileName(FILE_NAME);
        dto.setBlobPath(BLOB_PATH);
        dto.setFileSize(FILE_SIZE);
        dto.setContentHash(FILE_HASH);
        dto.setRetentionDays(365);
        dto.setCreatedBy("system");
        dto.setCreatedAt(LocalDateTime.now().minusDays(1));
        return dto;
    }

    public static StatementDTO validStatementDtoWithoutId() {
        StatementDTO dto = validStatementDto();
        dto.setId(null);
        return dto;
    }

    // =====================================================
    // STATEMENT ENTITY FIXTURES
    // =====================================================

    public static StatementEntity validStatementEntity() {
        StatementEntity entity = new StatementEntity();
        entity.setId(VALID_ID);
        entity.setCustomerId(CUSTOMER_ID);
        entity.setAccountNumber(ACCOUNT_NUMBER);
        entity.setStatementDate(LocalDateTime.of(2024, 1, 31, 0, 0));
        entity.setStatementType(DEFAULT_TYPE);
        entity.setStatus(DEFAULT_STATUS);
        entity.setFileName(FILE_NAME);
        entity.setBlobPath(BLOB_PATH);
        entity.setFileSize(FILE_SIZE);
        entity.setContentHash(FILE_HASH);
        entity.setRetentionDays(365);
        entity.setCreatedBy("system");
        entity.setCreatedAt(LocalDateTime.now().minusDays(1));
        return entity;
    }

    // =====================================================
    // DOWNLOAD LINK DTO FIXTURES
    // =====================================================

    public static DownloadLinkDTO validDownloadLinkDto() {
        DownloadLinkDTO dto = new DownloadLinkDTO();
        dto.setId(ANOTHER_VALID_ID);
        dto.setStatementId(VALID_ID);
        dto.setToken("dGVzdC10b2tlbg");
        dto.setExpiresAt(LocalDateTime.now().plusHours(24));
        dto.setMaxDownloads(3);
        dto.setDownloadCount(0);
        dto.setStatus(DownloadLinkStatusCode.ACTIVE);
        dto.setCreatedBy("system");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setDownloadUrl("http://localhost:8093/api/v1/statements/download/dGVzdC10b2tlbg");
        return dto;
    }

    // =====================================================
    // DOWNLOAD LINK ENTITY FIXTURES
    // =====================================================

    public static DownloadLinkEntity validDownloadLinkEntity() {
        DownloadLinkEntity entity = new DownloadLinkEntity();
        entity.setId(ANOTHER_VALID_ID);
        entity.setStatementId(VALID_ID);
        entity.setToken("dGVzdC10b2tlbg");
        entity.setExpiresAt(LocalDateTime.now().plusHours(24));
        entity.setMaxDownloads(3);
        entity.setDownloadCount(0);
        entity.setStatus(DownloadLinkStatusCode.ACTIVE);
        entity.setCreatedBy("system");
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    // =====================================================
    // REQUEST FIXTURES
    // =====================================================

    public static CreateDownloadLinkRequestDTO validCreateDownloadLinkRequest() {
        CreateDownloadLinkRequestDTO request = new CreateDownloadLinkRequestDTO();
        request.setExpiresInHours(24);
        request.setMaxDownloads(3);
        return request;
    }

    // =====================================================
    // LIST FIXTURES
    // =====================================================

    public static List<StatementDTO> statementDtoList(int count) {
        List<StatementDTO> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            StatementDTO dto = validStatementDto();
            dto.setId(UUID.randomUUID().toString());
            list.add(dto);
        }
        return list;
    }
}

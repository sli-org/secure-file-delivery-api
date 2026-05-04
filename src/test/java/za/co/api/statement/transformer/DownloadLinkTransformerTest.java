package za.co.api.statement.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import za.co.api.statement.dto.DownloadLinkDTO;
import za.co.api.statement.dto.code.DownloadLinkStatusCode;
import za.co.api.statement.entity.DownloadLinkEntity;
import za.co.api.statement.test.base.BaseTransformerTest;
import za.co.api.statement.test.data.StatementTestFixtures;

@Tag("downloadlink")
@DisplayName("[SFD-220] DownloadLink Transformer Tests")
class DownloadLinkTransformerTest extends BaseTransformerTest {

    private DownloadLinkTransformer transformer;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        transformer = new DownloadLinkTransformer();
    }

    @Test
    @DisplayName("[SFD-221] Should transform entity to DTO with all fields mapped")
    void whenToDTO_thenMapsAllFields() {
        DownloadLinkEntity entity = StatementTestFixtures.validDownloadLinkEntity();

        DownloadLinkDTO result = transformer.toDTO(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entity.getId());
        assertThat(result.getStatementId()).isEqualTo(entity.getStatementId());
        assertThat(result.getToken()).isEqualTo(entity.getToken());
        assertThat(result.getMaxDownloads()).isEqualTo(entity.getMaxDownloads());
        assertThat(result.getDownloadCount()).isEqualTo(entity.getDownloadCount());
        assertThat(result.getStatus()).isEqualTo(entity.getStatus());
    }

    @Test
    @DisplayName("[SFD-222] Should return null for null entity")
    void whenToDTO_withNull_thenReturnsNull() {
        assertNullInputReturnsNull(transformer.toDTO(null), "toDTO");
    }

    @Test
    @DisplayName("[SFD-223] Should transform DTO to entity with all fields mapped")
    void whenToEntity_thenMapsAllFields() {
        DownloadLinkDTO dto = StatementTestFixtures.validDownloadLinkDto();

        DownloadLinkEntity result = transformer.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getStatementId()).isEqualTo(dto.getStatementId());
        assertThat(result.getToken()).isEqualTo(dto.getToken());
        assertThat(result.getMaxDownloads()).isEqualTo(dto.getMaxDownloads());
        assertThat(result.getStatus()).isEqualTo(dto.getStatus());
    }

    @Test
    @DisplayName("[SFD-224] Should return null for null DTO")
    void whenToEntity_withNull_thenReturnsNull() {
        assertNullInputReturnsNull(transformer.toEntity(null), "toEntity");
    }
}

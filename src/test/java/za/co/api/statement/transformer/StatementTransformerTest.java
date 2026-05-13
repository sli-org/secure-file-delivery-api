package za.co.api.statement.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import za.co.api.statement.dto.StatementDTO;
import za.co.api.statement.entity.StatementEntity;
import za.co.api.statement.test.base.BaseTransformerTest;
import za.co.api.statement.test.data.StatementTestFixtures;

@Tag("statement")
@DisplayName("[SFD-200] Statement Transformer Tests")
class StatementTransformerTest extends BaseTransformerTest {

    private StatementTransformer transformer;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        transformer = new StatementTransformer();
    }

    @Nested
    @DisplayName("toDTO Tests")
    class ToDTOTests {

        @Test
        @DisplayName("[SFD-201] Should transform entity to DTO with all fields mapped")
        void whenToDTO_thenMapsAllFields() {
            StatementEntity entity = StatementTestFixtures.validStatementEntity();

            StatementDTO result = transformer.toDTO(entity);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(entity.getId());
            assertThat(result.getCustomerId()).isEqualTo(entity.getCustomerId());
            assertThat(result.getStatementDate()).isEqualTo(entity.getStatementDate());
            assertThat(result.getStatementType()).isEqualTo(entity.getStatementType());
            assertThat(result.getFileName()).isEqualTo(entity.getFileName());
            assertThat(result.getFileSize()).isEqualTo(entity.getFileSize());
            assertThat(result.getBlobPath()).isEqualTo(entity.getBlobPath());
            assertThat(result.getStatus()).isEqualTo(entity.getStatus());
            assertThat(result.getRetentionDays()).isEqualTo(entity.getRetentionDays());
        }

        @Test
        @DisplayName("[SFD-202] Should mask account number in DTO (BR4)")
        void whenToDTO_thenMasksAccountNumber() {
            StatementEntity entity = StatementTestFixtures.validStatementEntity();
            entity.setAccountNumber("1234567890");

            StatementDTO result = transformer.toDTO(entity);

            assertThat(result.getAccountNumber()).isEqualTo("****7890");
        }

        @Test
        @DisplayName("[SFD-203] Should return null for null entity")
        void whenToDTO_withNull_thenReturnsNull() {
            StatementDTO result = transformer.toDTO(null);
            assertNullInputReturnsNull(result, "toDTO");
        }
    }

    @Nested
    @DisplayName("toEntity Tests")
    class ToEntityTests {

        @Test
        @DisplayName("[SFD-204] Should transform DTO to entity with all fields mapped")
        void whenToEntity_thenMapsAllFields() {
            StatementDTO dto = StatementTestFixtures.validStatementDto();

            StatementEntity result = transformer.toEntity(dto);

            assertThat(result).isNotNull();
            assertThat(result.getCustomerId()).isEqualTo(dto.getCustomerId());
            assertThat(result.getStatementDate()).isEqualTo(dto.getStatementDate());
            assertThat(result.getStatementType()).isEqualTo(dto.getStatementType());
            assertThat(result.getFileName()).isEqualTo(dto.getFileName());
        }

        @Test
        @DisplayName("[SFD-205] Should return null for null DTO")
        void whenToEntity_withNull_thenReturnsNull() {
            StatementEntity result = transformer.toEntity(null);
            assertNullInputReturnsNull(result, "toEntity");
        }
    }

    @Nested
    @DisplayName("maskAccountNumber Tests")
    class MaskAccountNumberTests {

        @Test
        @DisplayName("[SFD-206] Should mask long account numbers showing last 4 digits")
        void whenMask_withLongNumber_thenShowsLast4() {
            assertThat(transformer.maskAccountNumber("1234567890")).isEqualTo("****7890");
        }

        @Test
        @DisplayName("[SFD-207] Should return short account numbers as-is")
        void whenMask_withShortNumber_thenReturnsAsIs() {
            assertThat(transformer.maskAccountNumber("1234")).isEqualTo("1234");
        }

        @Test
        @DisplayName("[SFD-208] Should return null for null input")
        void whenMask_withNull_thenReturnsNull() {
            assertThat(transformer.maskAccountNumber(null)).isNull();
        }
    }
}

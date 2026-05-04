package za.co.api.statement.dto.code;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("[SFD-STS] StatementStatusCode Tests")
class StatementStatusCodeTest {

    @Nested
    @DisplayName("Enum Values Definition")
    class EnumValuesDefinitionTests {

        @Test
        @DisplayName("[SFD-STS-001] Should have 3 enum values")
        void shouldHaveExpectedNumberOfValues() {
            assertThat(StatementStatusCode.values()).hasSize(3);
        }

        @ParameterizedTest
        @EnumSource(StatementStatusCode.class)
        @DisplayName("[SFD-STS-002] Should have non-null value for all enum constants")
        void shouldHaveNonNullValue(StatementStatusCode statusCode) {
            assertThat(statusCode.getValue()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("fromValue() Method Tests")
    class FromValueTests {

        @Test
        @DisplayName("[SFD-STS-010] Should return AVAILABLE from value")
        void shouldReturnAvailableFromValue() {
            assertThat(StatementStatusCode.fromValue("AVAILABLE")).isEqualTo(StatementStatusCode.AVAILABLE);
        }

        @Test
        @DisplayName("[SFD-STS-011] Should return EXPIRED from value")
        void shouldReturnExpiredFromValue() {
            assertThat(StatementStatusCode.fromValue("EXPIRED")).isEqualTo(StatementStatusCode.EXPIRED);
        }

        @Test
        @DisplayName("[SFD-STS-012] Should return DELETED from value")
        void shouldReturnDeletedFromValue() {
            assertThat(StatementStatusCode.fromValue("DELETED")).isEqualTo(StatementStatusCode.DELETED);
        }

        @Test
        @DisplayName("[SFD-STS-013] Should match case-insensitively")
        void shouldMatchCaseInsensitively() {
            assertThat(StatementStatusCode.fromValue("available")).isEqualTo(StatementStatusCode.AVAILABLE);
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "AVAIL", "unknown"})
        @DisplayName("[SFD-STS-014] Should throw for invalid values")
        void shouldThrowForInvalidValue(String invalidValue) {
            assertThatThrownBy(() -> StatementStatusCode.fromValue(invalidValue))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("[SFD-STS-015] Should throw for null or empty value")
        void shouldThrowForNullOrEmpty(String value) {
            assertThatThrownBy(() -> StatementStatusCode.fromValue(value))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("fromExternalCode() Method Tests")
    class FromExternalCodeTests {

        @Test
        @DisplayName("[SFD-STS-020] Should return AVAILABLE from external code AVAIL")
        void shouldReturnAvailableFromExternalCode() {
            assertThat(StatementStatusCode.fromExternalCode("AVAIL")).isEqualTo(StatementStatusCode.AVAILABLE);
        }

        @Test
        @DisplayName("[SFD-STS-021] Should return null for null external code")
        void shouldReturnNullForNullExternalCode() {
            assertThat(StatementStatusCode.fromExternalCode(null)).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "avail", "AVAILABLE", ""})
        @DisplayName("[SFD-STS-022] Should throw for invalid external code")
        void shouldThrowForInvalidExternalCode(String invalidCode) {
            assertThatThrownBy(() -> StatementStatusCode.fromExternalCode(invalidCode))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("No StatementStatusCode found for external code: " + invalidCode);
        }
    }

    @Nested
    @DisplayName("Bidirectional Mapping")
    class BidirectionalMappingTests {

        @ParameterizedTest
        @EnumSource(StatementStatusCode.class)
        @DisplayName("[SFD-STS-030] Should round-trip through getValue() and fromValue()")
        void shouldRoundTrip(StatementStatusCode statusCode) {
            assertThat(StatementStatusCode.fromValue(statusCode.getValue())).isEqualTo(statusCode);
        }
    }
}

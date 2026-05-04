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

@DisplayName("[SFD-DLS] DownloadLinkStatusCode Tests")
class DownloadLinkStatusCodeTest {

    @Nested
    @DisplayName("Enum Values Definition")
    class EnumValuesDefinitionTests {

        @Test
        @DisplayName("[SFD-DLS-001] Should have 4 enum values")
        void shouldHaveExpectedNumberOfValues() {
            assertThat(DownloadLinkStatusCode.values()).hasSize(4);
        }

        @ParameterizedTest
        @EnumSource(DownloadLinkStatusCode.class)
        @DisplayName("[SFD-DLS-002] Should have non-null value for all enum constants")
        void shouldHaveNonNullValue(DownloadLinkStatusCode statusCode) {
            assertThat(statusCode.getValue()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("fromValue() Method Tests")
    class FromValueTests {

        @Test
        @DisplayName("[SFD-DLS-010] Should return ACTIVE from value")
        void shouldReturnActiveFromValue() {
            assertThat(DownloadLinkStatusCode.fromValue("ACTIVE")).isEqualTo(DownloadLinkStatusCode.ACTIVE);
        }

        @Test
        @DisplayName("[SFD-DLS-011] Should return USED from value")
        void shouldReturnUsedFromValue() {
            assertThat(DownloadLinkStatusCode.fromValue("USED")).isEqualTo(DownloadLinkStatusCode.USED);
        }

        @Test
        @DisplayName("[SFD-DLS-012] Should return EXPIRED from value")
        void shouldReturnExpiredFromValue() {
            assertThat(DownloadLinkStatusCode.fromValue("EXPIRED")).isEqualTo(DownloadLinkStatusCode.EXPIRED);
        }

        @Test
        @DisplayName("[SFD-DLS-013] Should return REVOKED from value")
        void shouldReturnRevokedFromValue() {
            assertThat(DownloadLinkStatusCode.fromValue("REVOKED")).isEqualTo(DownloadLinkStatusCode.REVOKED);
        }

        @Test
        @DisplayName("[SFD-DLS-014] Should match case-insensitively")
        void shouldMatchCaseInsensitively() {
            assertThat(DownloadLinkStatusCode.fromValue("active")).isEqualTo(DownloadLinkStatusCode.ACTIVE);
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "ACT", "unknown"})
        @DisplayName("[SFD-DLS-015] Should throw for invalid values")
        void shouldThrowForInvalidValue(String invalidValue) {
            assertThatThrownBy(() -> DownloadLinkStatusCode.fromValue(invalidValue))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("[SFD-DLS-016] Should throw for null or empty value")
        void shouldThrowForNullOrEmpty(String value) {
            assertThatThrownBy(() -> DownloadLinkStatusCode.fromValue(value))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("fromExternalCode() Method Tests")
    class FromExternalCodeTests {

        @Test
        @DisplayName("[SFD-DLS-020] Should return ACTIVE from external code ACT")
        void shouldReturnActiveFromExternalCode() {
            assertThat(DownloadLinkStatusCode.fromExternalCode("ACT")).isEqualTo(DownloadLinkStatusCode.ACTIVE);
        }

        @Test
        @DisplayName("[SFD-DLS-021] Should return null for null external code")
        void shouldReturnNullForNullExternalCode() {
            assertThat(DownloadLinkStatusCode.fromExternalCode(null)).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "act", "ACTIVE", ""})
        @DisplayName("[SFD-DLS-022] Should throw for invalid external code")
        void shouldThrowForInvalidExternalCode(String invalidCode) {
            assertThatThrownBy(() -> DownloadLinkStatusCode.fromExternalCode(invalidCode))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("No DownloadLinkStatusCode found for external code: " + invalidCode);
        }
    }

    @Nested
    @DisplayName("Bidirectional Mapping")
    class BidirectionalMappingTests {

        @ParameterizedTest
        @EnumSource(DownloadLinkStatusCode.class)
        @DisplayName("[SFD-DLS-030] Should round-trip through getValue() and fromValue()")
        void shouldRoundTrip(DownloadLinkStatusCode statusCode) {
            assertThat(DownloadLinkStatusCode.fromValue(statusCode.getValue())).isEqualTo(statusCode);
        }
    }
}

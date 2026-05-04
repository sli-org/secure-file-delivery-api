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

@DisplayName("[SFD-ENM] StatementTypeCode Tests")
class StatementTypeCodeTest {

    @Nested
    @DisplayName("Enum Values Definition")
    class EnumValuesDefinitionTests {

        @Test
        @DisplayName("[SFD-ENM-001] Should have 4 enum values")
        void shouldHaveExpectedNumberOfValues() {
            assertThat(StatementTypeCode.values()).hasSize(4);
        }

        @ParameterizedTest
        @EnumSource(StatementTypeCode.class)
        @DisplayName("[SFD-ENM-002] Should have non-null value for all enum constants")
        void shouldHaveNonNullValue(StatementTypeCode typeCode) {
            assertThat(typeCode.getValue()).isNotNull().isNotEmpty();
        }

        @ParameterizedTest
        @EnumSource(StatementTypeCode.class)
        @DisplayName("[SFD-ENM-003] Should have non-null description for all enum constants")
        void shouldHaveNonNullDescription(StatementTypeCode typeCode) {
            assertThat(typeCode.getDescription()).isNotNull().isNotEmpty();
        }

        @ParameterizedTest
        @EnumSource(StatementTypeCode.class)
        @DisplayName("[SFD-ENM-004] Should have non-null external code for all enum constants")
        void shouldHaveNonNullExternalCode(StatementTypeCode typeCode) {
            assertThat(typeCode.getExternalCode()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("fromValue() Method Tests")
    class FromValueTests {

        @Test
        @DisplayName("[SFD-ENM-010] Should return MONTHLY from value")
        void shouldReturnMonthlyFromValue() {
            assertThat(StatementTypeCode.fromValue("MONTHLY")).isEqualTo(StatementTypeCode.MONTHLY);
        }

        @Test
        @DisplayName("[SFD-ENM-011] Should return ANNUAL from value")
        void shouldReturnAnnualFromValue() {
            assertThat(StatementTypeCode.fromValue("ANNUAL")).isEqualTo(StatementTypeCode.ANNUAL);
        }

        @Test
        @DisplayName("[SFD-ENM-012] Should return TAX from value")
        void shouldReturnTaxFromValue() {
            assertThat(StatementTypeCode.fromValue("TAX")).isEqualTo(StatementTypeCode.TAX);
        }

        @Test
        @DisplayName("[SFD-ENM-013] Should return AD_HOC from value")
        void shouldReturnAdHocFromValue() {
            assertThat(StatementTypeCode.fromValue("AD_HOC")).isEqualTo(StatementTypeCode.AD_HOC);
        }

        @Test
        @DisplayName("[SFD-ENM-014] Should match case-insensitively")
        void shouldMatchCaseInsensitively() {
            assertThat(StatementTypeCode.fromValue("monthly")).isEqualTo(StatementTypeCode.MONTHLY);
            assertThat(StatementTypeCode.fromValue("Monthly")).isEqualTo(StatementTypeCode.MONTHLY);
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "MTH", "unknown"})
        @DisplayName("[SFD-ENM-015] Should throw for invalid values")
        void shouldThrowForInvalidValue(String invalidValue) {
            assertThatThrownBy(() -> StatementTypeCode.fromValue(invalidValue))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("[SFD-ENM-016] Should throw for null or empty value")
        void shouldThrowForNullOrEmpty(String value) {
            assertThatThrownBy(() -> StatementTypeCode.fromValue(value))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("fromExternalCode() Method Tests")
    class FromExternalCodeTests {

        @Test
        @DisplayName("[SFD-ENM-020] Should return MONTHLY from external code MTH")
        void shouldReturnMonthlyFromExternalCode() {
            assertThat(StatementTypeCode.fromExternalCode("MTH")).isEqualTo(StatementTypeCode.MONTHLY);
        }

        @Test
        @DisplayName("[SFD-ENM-021] Should return ANNUAL from external code ANN")
        void shouldReturnAnnualFromExternalCode() {
            assertThat(StatementTypeCode.fromExternalCode("ANN")).isEqualTo(StatementTypeCode.ANNUAL);
        }

        @Test
        @DisplayName("[SFD-ENM-022] Should return TAX from external code TAX")
        void shouldReturnTaxFromExternalCode() {
            assertThat(StatementTypeCode.fromExternalCode("TAX")).isEqualTo(StatementTypeCode.TAX);
        }

        @Test
        @DisplayName("[SFD-ENM-023] Should return AD_HOC from external code ADH")
        void shouldReturnAdHocFromExternalCode() {
            assertThat(StatementTypeCode.fromExternalCode("ADH")).isEqualTo(StatementTypeCode.AD_HOC);
        }

        @Test
        @DisplayName("[SFD-ENM-024] Should return null for null external code")
        void shouldReturnNullForNullExternalCode() {
            assertThat(StatementTypeCode.fromExternalCode(null)).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "mth", "MONTHLY", ""})
        @DisplayName("[SFD-ENM-025] Should throw for invalid external code")
        void shouldThrowForInvalidExternalCode(String invalidCode) {
            assertThatThrownBy(() -> StatementTypeCode.fromExternalCode(invalidCode))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("No StatementTypeCode found for external code: " + invalidCode);
        }
    }

    @Nested
    @DisplayName("Getter Methods Tests")
    class GetterMethodsTests {

        @Test
        @DisplayName("[SFD-ENM-030] Should return correct values for MONTHLY")
        void shouldReturnCorrectValuesForMonthly() {
            assertThat(StatementTypeCode.MONTHLY.getValue()).isEqualTo("MONTHLY");
            assertThat(StatementTypeCode.MONTHLY.getDescription()).isEqualTo("Monthly account statement");
            assertThat(StatementTypeCode.MONTHLY.getExternalCode()).isEqualTo("MTH");
        }

        @Test
        @DisplayName("[SFD-ENM-031] Should return value from toString()")
        void shouldReturnValueFromToString() {
            assertThat(StatementTypeCode.MONTHLY.toString()).isEqualTo("MONTHLY");
            assertThat(StatementTypeCode.TAX.toString()).isEqualTo("TAX");
        }
    }

    @Nested
    @DisplayName("Bidirectional Mapping")
    class BidirectionalMappingTests {

        @ParameterizedTest
        @EnumSource(StatementTypeCode.class)
        @DisplayName("[SFD-ENM-040] Should round-trip through getValue() and fromValue()")
        void shouldRoundTrip(StatementTypeCode typeCode) {
            String value = typeCode.getValue();
            StatementTypeCode result = StatementTypeCode.fromValue(value);
            assertThat(result).isEqualTo(typeCode);
        }
    }
}

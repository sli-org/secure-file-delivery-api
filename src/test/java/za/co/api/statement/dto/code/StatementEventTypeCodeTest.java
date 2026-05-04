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

@DisplayName("[SFD-EVT] StatementEventTypeCode Tests")
class StatementEventTypeCodeTest {

    @Nested
    @DisplayName("Enum Values Definition")
    class EnumValuesDefinitionTests {

        @Test
        @DisplayName("[SFD-EVT-001] Should have 3 event types")
        void shouldHaveAllExpectedEventTypes() {
            StatementEventTypeCode[] values = StatementEventTypeCode.values();
            assertThat(values).hasSize(3);
            assertThat(values).containsExactlyInAnyOrder(
                    StatementEventTypeCode.CREATED,
                    StatementEventTypeCode.UPDATED,
                    StatementEventTypeCode.DELETED
            );
        }

        @ParameterizedTest
        @EnumSource(StatementEventTypeCode.class)
        @DisplayName("[SFD-EVT-002] Should have non-null value for all event types")
        void shouldHaveNonNullValue(StatementEventTypeCode eventType) {
            assertThat(eventType.getValue()).isNotNull().isNotEmpty();
        }

        @ParameterizedTest
        @EnumSource(StatementEventTypeCode.class)
        @DisplayName("[SFD-EVT-003] Should have non-null description for all event types")
        void shouldHaveNonNullDescription(StatementEventTypeCode eventType) {
            assertThat(eventType.getDescription()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("fromValue() Method Tests")
    class FromValueTests {

        @Test
        @DisplayName("[SFD-EVT-010] Should return CREATED from value")
        void shouldReturnCreatedFromValue() {
            assertThat(StatementEventTypeCode.fromValue("CREATED")).isEqualTo(StatementEventTypeCode.CREATED);
        }

        @Test
        @DisplayName("[SFD-EVT-011] Should return UPDATED from value")
        void shouldReturnUpdatedFromValue() {
            assertThat(StatementEventTypeCode.fromValue("UPDATED")).isEqualTo(StatementEventTypeCode.UPDATED);
        }

        @Test
        @DisplayName("[SFD-EVT-012] Should return DELETED from value")
        void shouldReturnDeletedFromValue() {
            assertThat(StatementEventTypeCode.fromValue("DELETED")).isEqualTo(StatementEventTypeCode.DELETED);
        }

        @Test
        @DisplayName("[SFD-EVT-013] Should match case-insensitively")
        void shouldMatchCaseInsensitively() {
            assertThat(StatementEventTypeCode.fromValue("created")).isEqualTo(StatementEventTypeCode.CREATED);
            assertThat(StatementEventTypeCode.fromValue("CREATED")).isEqualTo(StatementEventTypeCode.CREATED);
            assertThat(StatementEventTypeCode.fromValue("Created")).isEqualTo(StatementEventTypeCode.CREATED);
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "CREATE", "DELETE", "unknown"})
        @DisplayName("[SFD-EVT-014] Should throw for invalid values")
        void shouldThrowForInvalidValue(String invalidValue) {
            assertThatThrownBy(() -> StatementEventTypeCode.fromValue(invalidValue))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("[SFD-EVT-015] Should throw for null or empty value")
        void shouldThrowForNullOrEmpty(String value) {
            assertThatThrownBy(() -> StatementEventTypeCode.fromValue(value))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Getter Methods Tests")
    class GetterMethodsTests {

        @Test
        @DisplayName("[SFD-EVT-020] Should return correct values")
        void shouldReturnCorrectValues() {
            assertThat(StatementEventTypeCode.CREATED.getValue()).isEqualTo("CREATED");
            assertThat(StatementEventTypeCode.UPDATED.getValue()).isEqualTo("UPDATED");
            assertThat(StatementEventTypeCode.DELETED.getValue()).isEqualTo("DELETED");
        }

        @Test
        @DisplayName("[SFD-EVT-021] Should return value from toString()")
        void shouldReturnValueFromToString() {
            assertThat(StatementEventTypeCode.CREATED.toString()).isEqualTo("CREATED");
        }
    }

    @Nested
    @DisplayName("Bidirectional Mapping")
    class BidirectionalMappingTests {

        @ParameterizedTest
        @EnumSource(StatementEventTypeCode.class)
        @DisplayName("[SFD-EVT-030] Should round-trip through getValue() and fromValue()")
        void shouldRoundTrip(StatementEventTypeCode eventType) {
            assertThat(StatementEventTypeCode.fromValue(eventType.getValue())).isEqualTo(eventType);
        }
    }
}

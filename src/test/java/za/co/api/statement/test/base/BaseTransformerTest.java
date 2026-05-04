package za.co.api.statement.test.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public abstract class BaseTransformerTest {

    protected ObjectMapper objectMapper;

    @BeforeEach
    protected void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    protected void assertNullInputReturnsNull(Object result, String methodName) {
        assertThat(result)
                .as("%s should return null when given null input", methodName)
                .isNull();
    }

    protected void assertAuditFieldsNotModified(
            String originalCreatedBy, java.time.LocalDateTime originalCreatedAt,
            String currentCreatedBy, java.time.LocalDateTime currentCreatedAt) {
        assertThat(currentCreatedBy).isEqualTo(originalCreatedBy);
        assertThat(currentCreatedAt).isEqualTo(originalCreatedAt);
    }
}

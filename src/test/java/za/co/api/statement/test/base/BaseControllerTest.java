package za.co.api.statement.test.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import za.co.common.exception.GlobalExceptionHandler;
import za.co.common.test.annotation.ApiControllerTest;

@ApiControllerTest
@ExtendWith(MockitoExtension.class)
public abstract class BaseControllerTest {

    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;

    @BeforeEach
    protected void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders
                .standaloneSetup(getController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    protected abstract Object getController();
}

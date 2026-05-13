package za.co.api.statement.test.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import io.sentry.Sentry;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;
import za.co.common.exception.GlobalExceptionHandler;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public abstract class BaseControllerTest {

    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private MockedStatic<Sentry> sentryMock;
    protected abstract Object getController();

    @BeforeEach
    void setUpMockMvc() {

    sentryMock = Mockito.mockStatic(Sentry.class);
    sentryMock.when(() -> Sentry.captureException(any())).thenAnswer(inv -> null);

        mockMvc = MockMvcBuilders
                .standaloneSetup(getController())
                // Register your GlobalExceptionHandler here so 404s etc. work:
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Populate SecurityContext so @PreAuthorize passes AND
        // Authentication is injected into controller method parameters
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "test-user", null,
                        List.of(new SimpleGrantedAuthority("ROLE_common-api.admin"))
                );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        sentryMock.close();
    }
}
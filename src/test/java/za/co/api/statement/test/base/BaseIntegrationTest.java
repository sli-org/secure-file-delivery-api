package za.co.api.statement.test.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import za.co.api.statement.test.config.IntegrationTestSecurityConfig;
import za.co.common.test.annotation.ApiIntegrationTest;

/**
 * Base class for integration tests with real HTTP calls.
 * Provides TestRestTemplate, mock RabbitMQ, and synthetic JWT auth headers.
 */
@ApiIntegrationTest
@ActiveProfiles("test")
@Import(IntegrationTestSecurityConfig.class)
public abstract class BaseIntegrationTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    @Qualifier("CommonRabbitTemplate")
    protected RabbitTemplate commonRabbitTemplate;

    @LocalServerPort
    protected int port;

    protected String baseUrl;
    protected HttpHeaders authJsonHeaders;
    protected HttpHeaders authMultipartHeaders;

    private static final String ADMIN_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0In0.fullscope-admin";

    @BeforeEach
    void baseIntegrationSetup() {
        baseUrl = "http://localhost:" + port + "/api/v1/statements";

        authJsonHeaders = new HttpHeaders();
        authJsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        authJsonHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_TOKEN);

        authMultipartHeaders = new HttpHeaders();
        authMultipartHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        authMultipartHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_TOKEN);
    }

    protected String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
}

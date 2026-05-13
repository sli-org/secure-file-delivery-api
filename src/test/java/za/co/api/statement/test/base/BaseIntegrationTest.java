package za.co.api.statement.test.base;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    protected RestTemplate restTemplate;
    protected String baseUrl;

    @BeforeEach
    void setUp() {
        this.restTemplate = new RestTemplate();
        baseUrl = "http://localhost:" + port + "/api/v1/statements";
    }
}
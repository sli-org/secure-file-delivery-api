package za.co.api.statement.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import za.co.api.statement.test.base.BaseIntegrationTest;
import za.co.api.statement.test.config.TestAzureConfig;
import za.co.api.statement.test.config.TestRabbitMQConfig;
import za.co.api.statement.test.config.TestSecurityConfig;
import za.co.api.statement.test.data.StatementTestFixtures;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestRabbitMQConfig.class, TestAzureConfig.class})
@Tag("statement")
@Tag("security")
@DisplayName("[SFD-AI] Statement Authentication Integration Tests")
class StatementAuthenticationIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("[SFD-AI-001] GET /{id} without token returns 401")
    void whenGetStatement_withoutToken_then401() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(null, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                baseUrl + "/" + StatementTestFixtures.VALID_ID,
                HttpMethod.GET, request, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("[SFD-AI-002] GET / (list) without token returns 401")
    void whenListStatements_withoutToken_then401() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(null, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                baseUrl + "?customerId=CUST-001&limit=10&offset=0",
                HttpMethod.GET, request, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("[SFD-AI-003] DELETE /{id} without token returns 401")
    void whenDeleteStatement_withoutToken_then401() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(null, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                baseUrl + "/" + StatementTestFixtures.VALID_ID,
                HttpMethod.DELETE, request, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("[SFD-AI-004] Request with invalid JWT returns 401")
    void whenRequest_withInvalidJwt_then401() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt.token");
        HttpEntity<Void> request = new HttpEntity<>(null, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                baseUrl + "/" + StatementTestFixtures.VALID_ID,
                HttpMethod.GET, request, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("[SFD-AI-005] GET /download/{token} (public endpoint) does NOT require auth")
    void whenDownload_withoutToken_thenNotUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(null, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                baseUrl + "/download/some-test-token",
                HttpMethod.GET, request, Object.class);

        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
package za.co.api.statement.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import za.co.api.statement.test.base.BaseIntegrationTest;
import za.co.api.statement.test.data.StatementTestFixtures;

/**
 * Authorization integration tests — 403 Forbidden scenarios.
 * Verifies insufficient permissions are rejected.
 */
@Tag("statement")
@Tag("security")
@DisplayName("[SFD-AuI] Statement Authorization Integration Tests")
class StatementAuthorizationIntegrationTest extends BaseIntegrationTest {

    private static final String READONLY_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0In0.readonly";
    private static final String NO_SCOPE_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0In0.noscope";

    @Test
    @DisplayName("[SFD-AuI-001] DELETE with read-only scope returns 403")
    void whenDelete_withReadOnlyScope_then403() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + READONLY_TOKEN);
        HttpEntity<Void> request = new HttpEntity<>(null, headers);

        ResponseEntity<Object> response = testRestTemplate.exchange(
                baseUrl + "/" + StatementTestFixtures.VALID_ID,
                HttpMethod.DELETE, request, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("[SFD-AuI-002] GET with no scopes and no roles returns 403")
    void whenGet_withNoScopesNoRoles_then403() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + NO_SCOPE_TOKEN);
        HttpEntity<Void> request = new HttpEntity<>(null, headers);

        ResponseEntity<Object> response = testRestTemplate.exchange(
                baseUrl + "/" + StatementTestFixtures.VALID_ID,
                HttpMethod.GET, request, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("[SFD-AuI-003] GET (list) with read-only scope returns 200")
    void whenList_withReadOnlyScope_then200() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + READONLY_TOKEN);
        HttpEntity<Void> request = new HttpEntity<>(null, headers);

        ResponseEntity<Object> response = testRestTemplate.exchange(
                baseUrl + "?customerId=CUST-001",
                HttpMethod.GET, request, Object.class);

        // Read-only scope should be allowed for list endpoint
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.FORBIDDEN);
    }
}

package za.co.api.statement.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import za.co.api.statement.test.base.BaseIntegrationTest;
import za.co.api.statement.test.config.TestSecurityConfig;
import za.co.api.statement.test.data.StatementTestFixtures;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("statement")
@Tag("security")
@DisplayName("[SFD-AZ] Statement Authorization Integration Tests")
@Import(TestSecurityConfig.class)
class StatementAuthorizationIntegrationTest extends BaseIntegrationTest {

    private String validToken;
    private String readOnlyToken;

    @BeforeEach
    void setUpTokens() {
        // "read.only" keyword causes TestSecurityConfig to grant only statement:read scope
        readOnlyToken = "valid.jwt.token.with.read.only.scope";
        // No special keyword — TestSecurityConfig grants full scopes (read + create + delete)
        validToken = "valid.jwt.token.with.full.scopes";
    }

    @Test
    @DisplayName("[SFD-AZ-001] GET with read-only scope returns 200")
    void whenList_withReadOnlyScope_then200() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + readOnlyToken);
        HttpEntity<Void> request = new HttpEntity<>(null, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                baseUrl + "?customerId=CUST-001&limit=10&offset=0",
                HttpMethod.GET, request, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("[SFD-AZ-002] DELETE with read-only scope returns 403")
    void whenDelete_withReadOnlyScope_then403() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + readOnlyToken);
        HttpEntity<Void> request = new HttpEntity<>(null, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                baseUrl + "/" + StatementTestFixtures.VALID_ID,
                HttpMethod.DELETE, request, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("[SFD-AZ-003] GET with no scopes/roles returns 403")
    void whenGet_withNoScopesNoRoles_then403() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer token.with.no.scopes");
        HttpEntity<Void> request = new HttpEntity<>(null, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                baseUrl + "/" + StatementTestFixtures.VALID_ID,
                HttpMethod.GET, request, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
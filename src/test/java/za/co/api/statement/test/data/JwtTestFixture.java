package za.co.api.statement.test.data;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Test fixture for creating JWT-based Authentication instances.
 */
public final class JwtTestFixture {

    private static final String DEFAULT_ISSUER = "https://login.microsoftonline.com/test-tenant/v2.0";
    private static final String DEFAULT_AUDIENCE = "test-audience-id";

    private JwtTestFixture() {
    }

    public static Authentication createAdminAuthentication() {
        Map<String, Object> claims = createBaseClaims();
        claims.put("roles", List.of("common-api.admin"));
        claims.put("scp", "statement:read statement:create statement:delete");
        Jwt jwt = buildJwt("admin-jwt-token", claims);
        return new JwtAuthenticationToken(jwt);
    }

    public static Authentication createUserAuthentication() {
        Map<String, Object> claims = createBaseClaims();
        claims.put("roles", List.of("statement-api.user"));
        claims.put("scp", "statement:read statement:create statement:delete");
        Jwt jwt = buildJwt("user-jwt-token", claims);
        return new JwtAuthenticationToken(jwt);
    }

    public static Authentication createReadOnlyAuthentication() {
        Map<String, Object> claims = createBaseClaims();
        claims.put("roles", List.of("statement-api.readonly"));
        claims.put("scp", "statement:read");
        Jwt jwt = buildJwt("readonly-jwt-token", claims);
        return new JwtAuthenticationToken(jwt);
    }

    public static Authentication createLimitedScopeAuthentication(String scope) {
        Map<String, Object> claims = createBaseClaims();
        claims.put("roles", List.of("statement-api.user"));
        claims.put("scp", scope);
        Jwt jwt = buildJwt("limited-scope-jwt-token", claims);
        return new JwtAuthenticationToken(jwt);
    }

    private static Map<String, Object> createBaseClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "test-user");
        claims.put("iss", DEFAULT_ISSUER);
        claims.put("aud", DEFAULT_AUDIENCE);
        claims.put("iat", Instant.now());
        claims.put("exp", Instant.now().plusSeconds(3600));
        claims.put("azp", "test-client-id");
        return claims;
    }

    private static Jwt buildJwt(String tokenValue, Map<String, Object> claims) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        headers.put("typ", "JWT");

        Jwt.Builder builder = Jwt.withTokenValue(tokenValue)
                .headers(h -> h.putAll(headers))
                .claims(c -> c.putAll(claims));

        if (!claims.containsKey("iss")) {
            builder.claim("iss", DEFAULT_ISSUER);
        }
        if (!claims.containsKey("iat")) {
            builder.issuedAt(Instant.now());
        }
        if (!claims.containsKey("exp")) {
            builder.expiresAt(Instant.now().plusSeconds(3600));
        }

        return builder.build();
    }
}
